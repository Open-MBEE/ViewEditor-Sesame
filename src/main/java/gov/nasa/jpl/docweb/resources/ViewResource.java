package gov.nasa.jpl.docweb.resources;

import gov.nasa.jpl.docweb.concept.Comment;
import gov.nasa.jpl.docweb.concept.DocumentView;
import gov.nasa.jpl.docweb.concept.ModelElement;
import gov.nasa.jpl.docweb.concept.NamedElement;
import gov.nasa.jpl.docweb.concept.NonContainerElement;
import gov.nasa.jpl.docweb.concept.Paragraph;
import gov.nasa.jpl.docweb.concept.Property;
import gov.nasa.jpl.docweb.concept.Source;
import gov.nasa.jpl.docweb.concept.Table;
import gov.nasa.jpl.docweb.concept.Text;
import gov.nasa.jpl.docweb.concept.URI;
import gov.nasa.jpl.docweb.concept.User;
import gov.nasa.jpl.docweb.concept.Util;
import gov.nasa.jpl.docweb.concept.View;
import gov.nasa.jpl.docweb.services.CommentService;
import gov.nasa.jpl.docweb.services.UserService;
import gov.nasa.jpl.docweb.services.ViewService;
import gov.nasa.jpl.docweb.spring.LocalConnectionFactory;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value="/rest/views")
public class ViewResource {

	@Autowired
	private LocalConnectionFactory<ObjectConnection> connectionFactory;	
	
	@Autowired
	private ViewService viewService;
	
	@Autowired
	private CommentService commentService;
	
	@Autowired
	private UserService userService;
	
	private static Logger log = Logger.getLogger(ViewResource.class.getName());
	
	/**
	 * <p>changed=true: returns all changed elements in the view/recursively</p>
	 * <p>[{"mdid": mdid, "name": blah, "documentation": blah, ...}, ...]</p> 
	 * <p>changed=false: returns all view information/recursively (not used right now)</p>
	 * <p>{
	 *		"views": [{"mdid": mdid, "contains": [{"type": blah, "source": source id, "useProperty": doc/name},...],
	 *		"view2view": {viewid: [viewid, ...], ...},
	 *		"elements": {mdid: {"name": name, "documentation":doc, ...}, ... }
	 *	}</p>
	 * @param viewid
	 * @param recurse
	 * @param changed if true, only gets uncommitted elements
	 * @return json depends on the values of recurse and changed
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 */
	@Transactional(readOnly=true)
	@RequestMapping(value="/{viewid}", method=RequestMethod.GET, produces="application/JSON;charset=UTF-8")
	public @ResponseBody String getView(@PathVariable("viewid") String viewid,
			@RequestParam(value="recurse", defaultValue="false") boolean recurse,
			@RequestParam(value="changed", defaultValue="false") boolean changed) throws RepositoryException, QueryEvaluationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		try {
			View v = oc.getObject(View.class, URI.DATA + viewid);
			if (changed) {
				return viewService.getChangedElements(v, recurse).toJSONString();
			} else
				return viewService.getViewJSON(v, recurse).toJSONString();
		} catch (ClassCastException e) {
			return "NotFound";
		}
	}
	
	
	
	
	/**
	 * <p>body is a list of model mdid that has been imported by magicdraw side and accepted by user</p>
	 * <p>[mdid, ...]</p>
	 * <p>sets all model elements with sent ids to committed</p>
	 * @param body
	 * @return
	 * @throws RepositoryException
	 * @throws ParseException
	 * @throws QueryEvaluationException
	 */
	@Transactional
	@RequestMapping(value="/committed", method=RequestMethod.POST)
	public @ResponseBody String commit(@RequestBody String body) throws RepositoryException, ParseException, QueryEvaluationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		log.info("committed: " + body);
		JSONArray elements = (JSONArray)(new JSONParser()).parse(body);
		for (Object o: elements) {
			if (o instanceof String) {
				try {
					ModelElement e = oc.getObject(ModelElement.class, URI.DATA + (String)o);
					e.setCommitted(true);
				} catch (ClassCastException e) {
				}
			}
		}
		return "ok";//Response.status(200).build();
	}
	
	
	/**
	 * <p>accepts body:</p>
	 * <p>{
	 *		"views": [see ViewService.updateOrCreateView],
	 *		"view2view": {viewid: [viewid, ...], ...},
	 *		"elements": [see ViewService.updateOrCreateModelElement]
	 *	}</p>
	 * @param viewid
	 * @param body
	 * @param force forces the database to look like the model, if not and there's any uncommitted changes, will return changed elements instead
	 * @param recurse whether there's a view2view key in the body
	 * @param doc whether the viewid is a DocumentView
	 * @return "ok" or [{"mdid": mdid, "type": "name"/"doc"}] if stuff have been merged, or "uncommitted changes" if there are changes and force is false
	 * @throws RepositoryException
	 * @throws ParseException
	 * @throws QueryEvaluationException
	 * @throws DatatypeConfigurationException
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	@RequestMapping(value="/{viewid}", method=RequestMethod.POST)
	public @ResponseBody String postView(@PathVariable("viewid") String viewid, 
			@RequestBody String body, 
			@RequestParam(value="force", defaultValue="false") boolean force, 
			@RequestParam(value="recurse", defaultValue="false") boolean recurse,
			@RequestParam(value="doc", defaultValue="false") boolean doc,
			@RequestParam("user") String user) throws RepositoryException, ParseException, QueryEvaluationException, DatatypeConfigurationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		log.info("posting view " + viewid + ": \n" + body);
		JSONObject view = (JSONObject)(new JSONParser()).parse(body);
		JSONArray elements = (JSONArray)view.get("elements");
		User u = userService.getOrCreateUser(user, oc);
		try {
			View v = null;
			ModelElement me = oc.getObject(ModelElement.class, URI.DATA + viewid);
			if (me instanceof View)
				v = (View)me;
			else
				v = oc.addDesignation(me, View.class);
			JSONArray changed = viewService.getChangedElements(v, recurse);
			if (!changed.isEmpty() && !force)
				return "uncommitted changes";
		} catch (ClassCastException e) {				
			if (doc) {
				DocumentView v = oc.addDesignation(oc.getObjectFactory().createObject(URI.DATA + viewid, DocumentView.class), DocumentView.class);
				v.setMdid(viewid);
				v.setCommitted(true);
				v.setModified(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)GregorianCalendar.getInstance()));
			} 
		}
		Set<String> merged = new HashSet<String>();
		for (Object e: elements) {
			JSONObject me = (JSONObject)e;
			viewService.updateOrCreateModelElement(me, oc, merged);
		}
		JSONArray views = (JSONArray)view.get("views");
		for (Object v: views) {
			JSONObject mv = (JSONObject)v;
			viewService.updateOrCreateView(mv, oc, u);
		}
		if (recurse) {
			JSONObject map = (JSONObject)view.get("view2view");
			for (String key: (Set<String>)map.keySet()) {
				View pv = oc.getObject(View.class, URI.DATA + key);
				List<String> children = (List<String>)map.get(key);
				Set<View> childviews = new HashSet<View>();
				int i = 1;
				for (String ckey: children) {
					View cv = oc.getObject(View.class, URI.DATA + ckey);
					cv.setIndex(i);
					cv.clearParents();
					childviews.add(cv);
					i++;
				}
				pv.setViews(childviews);
			}
		}
		String res = "ok";
		if (!merged.isEmpty()) {
			JSONArray mer = new JSONArray();
			for (String m: merged) {
				JSONObject o = new JSONObject();
				String id = m.split("-")[0];
				String type = m.split("-")[1];
				o.put("mdid", id);
				o.put("type", type);
				mer.add(o);
			}
			res = mer.toJSONString();
		}
		return res;//Response.status(200).build();
	}
	
	/**
	 * <p>accepts body:</p>
	 * <p>{"views": {viewid: [viewid, ...], ...},
	 *     "noSections": [mdid, ...]
	 *     }</p>
	 * @param viewid
	 * @return
	 * @throws RepositoryException
	 * @throws ParseException
	 * @throws QueryEvaluationException
	 * @throws DatatypeConfigurationException
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	@RequestMapping(value="/{viewid}/hierarchy", method=RequestMethod.POST)
	public @ResponseBody String postViewHierarchy(@PathVariable("viewid") String viewid, 
			@RequestBody String body) throws RepositoryException, ParseException, QueryEvaluationException, DatatypeConfigurationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		log.info("posting view hierarchy" + viewid + ": \n" + body);
		JSONObject input = (JSONObject)(new JSONParser()).parse(body);
		JSONObject view = (JSONObject)input.get("views");
		JSONArray nosections = (JSONArray)input.get("noSections");
		for (String parentView: (Set<String>)view.keySet()) {
			View parent = oc.getObject(View.class, URI.DATA + parentView);
			Set<View> children = new HashSet<View>();
			int index = 1;
			for (String cview: (List<String>)view.get(parentView)) {
				View child = oc.getObject(View.class, URI.DATA + cview);
				child.clearParents();
				children.add(child);
				child.setIndex(index);
				index++;
			}
			parent.setViews(children);
			if (nosections.contains(parent.getMdid()))
				parent.setNoSection(true);
		}
		return "ok";//Response.status(200).build();
	}
	
	/**
	 * <p>post:</p>
	 * <p>{
	 *		"view2comment": {viewid:[commentid], ...},
  	 *		"comments": [{"id": mdid, "body": body, "author": user, "modified": datetime}, ...]
	 *	}</p>
	 * @param viewid
	 * @param body
	 * @param recurse
	 * @return "ok" if everything's fine, NotFound if one of the views are not found
	 * @throws RepositoryException
	 * @throws ParseException
	 * @throws QueryEvaluationException
	 * @throws DatatypeConfigurationException
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	@RequestMapping(value="/{viewid}/comments", method=RequestMethod.POST) 
	public @ResponseBody String postComments(@PathVariable("viewid") String viewid,
			@RequestBody String body,
			@RequestParam(value="recurse", defaultValue="false") boolean recurse) throws RepositoryException, ParseException, QueryEvaluationException, DatatypeConfigurationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		log.info("posting comments " + viewid + ":\n" + body);
		Set<String> posted = new HashSet<String>();
		JSONObject ob = (JSONObject)(new JSONParser()).parse(body);
		JSONArray comments = (JSONArray)ob.get("comments");
		for (Object e: comments) {
			JSONObject me = (JSONObject)e;
			Comment com = commentService.updateOrCreateComment(me, oc);
			posted.add(com.getMdid());
			com.setCommitted(true);
		}
		JSONObject views = (JSONObject)ob.get("view2comment");
		for (String viewkey: (Set<String>)views.keySet()) {
			JSONArray commentids = (JSONArray)views.get(viewkey);
			try {
				View thisview = oc.getObject(View.class, URI.DATA + viewkey);
				for (Object commentid: commentids) {
					Comment c = oc.getObject(Comment.class, URI.DATA + (String)commentid);
					thisview.addComment(c);
				}
				for (Comment c: thisview.getComments()) {
					if (c.getCommitted() && !posted.contains(c.getMdid())) //if a comment was previously committed but is not posted here, it's deleted from magicdraw
						c.setDeleted(true);
				}
			} catch (ClassCastException ex) {
				return "NotFound";
			}
		}
		return "ok";//Response.status(200).build();
	}
	
	
	/**
	 * <p>returns only changed or new comments, new comments will have id that starts with "comment"</p>
	 * <p>{ "view2comment": {viewid: [commentid], ...},
  	 *	  "comments": [{"id": commentid, "body": body, "author": user, "modified": datetime, "deleted": true/false}, ...],
	 *	}</p>
	 * @param viewid
	 * @param recurse
	 * @return
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 */	
	@Transactional(readOnly=true)
	@RequestMapping(value="/{viewid}/comments", method=RequestMethod.GET, produces="application/JSON;charset=UTF-8")
	public @ResponseBody String getComments(@PathVariable("viewid") String viewid,
			@RequestParam(value="recurse", defaultValue="false") boolean recurse) throws RepositoryException, QueryEvaluationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		try {
			View v = oc.getObject(View.class, URI.DATA + viewid);
			return commentService.getChangedComments(v, recurse).toJSONString();
		} catch (ClassCastException e) {
			return "NotFound";
		}
	}
	
	
	
	/**
	 * this is just for me to toggle some element's commit status for testing before the web interface is finished
	 * @param mdid
	 * @param commit
	 * @return
	 * @throws RepositoryException 
	 * @throws QueryEvaluationException 
	 *
	 */
	@Transactional
	@RequestMapping(value="/commit/{mdid}", method=RequestMethod.GET)
	public @ResponseBody String toggleCommit(@PathVariable("mdid") String mdid, @RequestParam("commit") boolean commit) throws RepositoryException, QueryEvaluationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		try {
			ModelElement v = oc.getObject(ModelElement.class, URI.DATA + mdid);
			v.setCommitted(commit);
			return "ok";
		} catch (ClassCastException e) {
			return "unknown id";
		}
	}
	
	
}
