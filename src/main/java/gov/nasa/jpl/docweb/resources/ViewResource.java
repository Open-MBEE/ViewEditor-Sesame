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
				return getChangedElements(v, recurse).toJSONString();
			} else
				return getViewJSON(v, recurse).toJSONString();
		} catch (ClassCastException e) {
			return "{}";
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
	 *		"views": [{"mdid": mdid, "contains": [{"type": "Paragraph/...", "source": mdid, "useProperty": doc/name},...],
	 *		"view2view": {viewid: [viewid, ...], ...},
	 *		"elements": [{"mdid": mdid, "name": name, "documentation":doc, ...}, ... ]
	 *	}</p>
	 * @param viewid
	 * @param body
	 * @param force forces the database to look like the model, if not and there's any uncommitted changes, will return changed elements instead
	 * @param recurse whether there's a view2view key in the body
	 * @param doc whether the viewid is a DocumentView
	 * @return
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
		User u = getOrCreateUser(user, oc);
		try {
			View v = null;
			ModelElement me = oc.getObject(ModelElement.class, URI.DATA + viewid);
			if (me instanceof View)
				v = (View)me;
			else
				v = oc.addDesignation(me, View.class);
			JSONArray changed = getChangedElements(v, recurse);
			if (!changed.isEmpty() && !force)
				return changed.toJSONString();
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
			updateOrCreateModelElement(me, oc, merged);
		}
		JSONArray views = (JSONArray)view.get("views");
		for (Object v: views) {
			JSONObject mv = (JSONObject)v;
			updateOrCreateView(mv, oc, u);
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
			Comment com = updateOrCreateComment(me, oc);
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
			return getChangedComments(v, recurse).toJSONString();
		} catch (ClassCastException e) {
			return "NotFound";
		}
	}
	
	/**
	 * 
	 * @param e {"mdid": mdid, "name": name, "documentation": blah, "type": View/...}
	 * @param oc
	 * @return
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 * @throws DatatypeConfigurationException
	 */
	private ModelElement updateOrCreateModelElement(JSONObject e, ObjectConnection oc, Set<String> merged) throws RepositoryException, QueryEvaluationException, DatatypeConfigurationException {
		String id = (String)e.get("mdid");
		ModelElement res = null;
		boolean n = false;
		try {
			res = oc.getObject(ModelElement.class, URI.DATA + id);
		} catch (ClassCastException ex) {
			if (e.containsKey("type") && e.get("type").equals("View")) 
				res = oc.addDesignation(oc.getObjectFactory().createObject(URI.DATA + id, View.class), View.class);
			else if (e.containsKey("type") && e.get("type").equals("Property"))
				res = oc.addDesignation(oc.getObjectFactory().createObject(URI.DATA + id, Property.class), Property.class);
			else if (e.containsKey("type") && e.get("type").equals("Comment"))
				res = oc.addDesignation(oc.getObjectFactory().createObject(URI.DATA + id, Comment.class), Comment.class);
			else
				res = oc.addDesignation(oc.getObjectFactory().createObject(URI.DATA + id, NamedElement.class), NamedElement.class);
			n = true;
		}
		String name = null;
		if (res instanceof NamedElement)
			name = ((NamedElement)res).getName();
		String doc = res.getDocumentation();
		String dvalue = "";
		if (res instanceof Property)
			dvalue = ((Property)res).getDefaultValue();
		if ((name == null && e.containsKey("name") || e.containsKey("name") && !name.equals((String)e.get("name"))) ||
			(doc == null || e.containsKey("documentation") && !doc.equals((String)e.get("documentation"))) ||
			(dvalue == null && e.containsKey("dvalue") || e.containsKey("dvalue") && !dvalue.equals(e.get("dvalue")))) {
			if (!n) {
				RDFObject old = oc.getObjectFactory().createObject();
				ModelElement oldv = null;
				if (res instanceof View)
					oldv = oc.addDesignation(old, View.class);
				else if (res instanceof Property)
					oldv = oc.addDesignation(old, Property.class);
				else if (res instanceof Comment)
					oldv = oc.addDesignation(old, Comment.class);
				else
					oldv = oc.addDesignation(old, NamedElement.class);
				oldv.setMdid(id);
				oldv.setDocumentation(doc);
				if (res instanceof NamedElement)
					((NamedElement)oldv).setName(name);
				if (res instanceof Property)
					((Property)oldv).setDefaultValue(((Property)res).getDefaultValue());
				oldv.setCommitted(res.getCommitted());
				oldv.setModified(res.getModified());
				oldv.setOldVersion(res.getOldVersion());
				res.setOldVersion(oldv);
			}
			boolean committed = true;
			if (!res.getCommitted() && name != null && !name.equals((String)e.get("name"))) {
				((NamedElement)res).setName(name + " - MERGED - " + (String)e.get("name"));
				merged.add(id + "-name");
				committed = false;
			} else if (res instanceof NamedElement)
				((NamedElement)res).setName((String)e.get("name"));
			if (!res.getCommitted() && res.getDocumentation() != null && !res.getDocumentation().equals((String)e.get("documentation"))) {
				res.setDocumentation(res.getDocumentation() + " <p><strong><i> MERGED - NEED RESOLUTION! </i></strong></p> " + (String)e.get("documentation"));
				merged.add(id + "-doc");
				committed = false;
			} else
				res.setDocumentation((String)e.get("documentation"));
			if (res instanceof Property && e.containsKey("dvalue"))
				((Property)res).setDefaultValue((String)e.get("dvalue"));
			
			res.setCommitted(committed);
			res.setMdid(id);
			res.setModified(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)GregorianCalendar.getInstance()));
		}
		return res;
	}
	
	/**
	 * 
	 * @param view {"mdid": mdid, "noSection": true/false, "contains": [{"type": paragraph/..., "source": mdid, "sourceProperty": documentation/name/..}, ...]}
	 * @param oc
	 * @return
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 * @throws DatatypeConfigurationException
	 */
	private View updateOrCreateView(JSONObject view, ObjectConnection oc, User u) throws RepositoryException, QueryEvaluationException, DatatypeConfigurationException {
		String id = (String)view.get("mdid");
		View res = null;
		try {
			//if it was no section before but now is a section, want to make it a view instead of just namedelement
			NamedElement ne = oc.getObject(NamedElement.class, URI.DATA + id);
			if (ne instanceof View)
				res = (View)ne;
			else
				res = oc.addDesignation(ne, View.class);
		} catch (ClassCastException e) {
			res = oc.addDesignation(oc.getObjectFactory().createObject(URI.DATA + id, View.class), View.class);
			res.setMdid(id);
			res.setCommitted(true);
			res.setModified(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)GregorianCalendar.getInstance()));
		}
		List<NonContainerElement> nowcontains = res.getOrderedContainedElements(); //this should relaly be used to check against new so they don't get orphaned
		JSONArray contains = (JSONArray)view.get("contains");
		Set<NonContainerElement> newcontains = new HashSet<NonContainerElement>();
		int i = 1;
		for (Object o: contains) {
			JSONObject contained = (JSONObject)o;
			NonContainerElement c = createContainedElement(contained, i, oc);
			if (c != null) {
				newcontains.add(c);
				i++;
			}
		}
		if (view.containsKey("noSection"))
			res.setNoSection((Boolean)view.get("noSection"));
		res.setContains(newcontains);
		res.setLastModified(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)GregorianCalendar.getInstance()));
		res.setLastModifiedBy(u);
		return res;
	}
	
	/**
	 * @param o {"type": Paragraph/..., "source": mdid, "useProperty": DOCUMENTATION/NAME/..}
	 * @param index
	 * @param oc
	 * @return
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 */
	@SuppressWarnings("unchecked")
	private NonContainerElement createContainedElement(JSONObject o, int index, ObjectConnection oc) throws RepositoryException, QueryEvaluationException {
		String type = (String)o.get("type");
		NonContainerElement res = null;
		if (type.equals("Paragraph")) {
			RDFObject blah = oc.getObjectFactory().createObject();
			Paragraph p = oc.addDesignation(blah, Paragraph.class);
			String sid = (String)o.get("source");
			if (!sid.equals("text")) {
				ModelElement me = oc.getObject(ModelElement.class, URI.DATA + sid);
				p.addSource(me);
				String use = (String)o.get("useProperty");
				p.setUseProperty(use);
			} else {
				Text text = oc.addDesignation(oc.getObjectFactory().createObject(), Text.class);
				text.setText((String)o.get("text"));
				p.addSource(text);
			}
			p.setIndex(index);
			res = p;
		} else if (type.equals("Table")) {
			Set<Source> sources = new HashSet<Source>();
			Table t = oc.addDesignation(oc.getObjectFactory().createObject(), Table.class);
			t.setTitle((String)o.get("title"));
			t.setBody(o.get("body").toString());
			t.setHeader((String)o.get("header").toString());
			t.setStyle(o.get("style").toString());
			for (String mid: (List<String>)o.get("sources")) {
				ModelElement me = oc.getObject(ModelElement.class, URI.DATA + mid);
				sources.add(me);
			}
			t.setSources(sources);
			t.setIndex(index);
			res = t;
		}
		return res;
	}
	
	/**
	 * @param commentob {"id": mdid, "body": body, "author": user, "modified": datetime}
	 * @param oc
	 * @return
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 * @throws DatatypeConfigurationException
	 */
	public static Comment updateOrCreateComment(JSONObject commentob, ObjectConnection oc) throws RepositoryException, QueryEvaluationException, DatatypeConfigurationException {
		Comment comment = null;
		String id = (String)commentob.get("id");
		String user = (String)commentob.get("author");
		String modified = (String)commentob.get("modified");
		User u = null;
		try {
			comment = oc.getObject(Comment.class, URI.DATA + id);
		} catch (ClassCastException e) {
			comment = oc.addDesignation(oc.getObjectFactory().createObject(URI.DATA + id, Comment.class), Comment.class);
			comment.setMdid(id);
		} 
		if (comment != null) {
			u = getOrCreateUser(user, oc);
			comment.setAuthor(u);
			comment.setDocumentation((String)commentob.get("body"));
			Date mod;
			try {
				mod = Util.DATEFORMAT.parse(modified);
			} catch (java.text.ParseException e) {
				e.printStackTrace();
				mod = new Date();
			}
			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(mod);
			comment.setModified(DatatypeFactory.newInstance().newXMLGregorianCalendar(gc));
			comment.setDeleted(false);
		}
		return comment;
	}
	
	public static User getOrCreateUser(String username, ObjectConnection oc) throws RepositoryException, QueryEvaluationException {
		User u = null;
		try {
			u = oc.getObject(User.class, URI.USER + username);
		} catch (ClassCastException e) {
			u = oc.addDesignation(oc.getObjectFactory().createObject(URI.USER + username, User.class), User.class);
			u.setUserName(username);
		}
		return u;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject getViewJSON(View v, boolean recurse) {
		JSONObject elements = new JSONObject();
		JSONObject res = new JSONObject();
		JSONArray views = new JSONArray();
		views.add(getSingleViewJSON(v, elements));
		if (recurse) {
			Queue<View> queue = new LinkedList<View>();
			JSONObject view2view = new JSONObject();
			queue.add(v);
			while (!queue.isEmpty()) {
				View nv = queue.remove();
				JSONArray children = new JSONArray();
				for (View cv: nv.getOrderedChildrenViews()) {
					views.add(getSingleViewJSON(cv, elements));
					children.add(cv.getMdid());
					queue.add(cv);
				}
				view2view.put(nv.getMdid(), children);
			}
			res.put("view2view", view2view);
		}
		res.put("elements", elements);
		res.put("views", views);
		return res;
	}
	
	/**
	 * @param v
	 * @param elements any referenced element info will be added to this
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getSingleViewJSON(View v, JSONObject elements) {
		JSONObject res = new JSONObject();
		res.put("mdid", v.getMdid());
		JSONArray contains = new JSONArray();
		for (NonContainerElement c: v.getOrderedContainedElements()) {
			JSONObject contained = new JSONObject();
			if (c instanceof Paragraph) {
				contained.put("type", "Paragraph");
				Source s = ((Paragraph)c).getSources().iterator().next();
				if (s instanceof ModelElement) {
					String uuid = ((ModelElement)s).getMdid();
					contained.put("source", uuid);
					if (!elements.containsKey(uuid)) {
						JSONObject e = new JSONObject();
						e.put("documentation", ((ModelElement)s).getDocumentation());
						if (s instanceof NamedElement)
							e.put("name", ((NamedElement)s).getName());
						e.put("mdid", uuid);
						elements.put(uuid, e);
					}
				}
				contained.put("useProperty", ((Paragraph)c).getUseProperty());
			}
			contains.add(contained);
		}
		res.put("contains", contains);
		return res;
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray getChangedElements(View v, boolean recurse) {
		Set<ModelElement> res = null;
		JSONArray changed = new JSONArray();
		if (recurse)
			res = v.getUncommittedModelElementsRecursive();
		else
			res = v.getUncommittedModelElements();
		for (ModelElement me: res) {
			JSONObject element = new JSONObject();
			if (me instanceof NamedElement) {
				element.put("name", ((NamedElement)me).getName());
			}
			if (me instanceof Property) {
				element.put("dvalue", ((Property)me).getDefaultValue());
			}
			element.put("mdid", me.getMdid());
			element.put("documentation", me.getDocumentation());
			changed.add(element);
		}
		return changed;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject getChangedComments(View v, boolean recurse) {
		JSONObject res = new JSONObject();
		JSONArray comments = new JSONArray();
		JSONObject view2comment = new JSONObject();
		fillChangedComments(v, comments, view2comment);
		if (recurse) {
			for (View view: v.getChildrenViewsRecursive())
				fillChangedComments(view, comments, view2comment);
		}
		if (comments.isEmpty())
			return res;
		res.put("comments", comments);
		res.put("view2comment", view2comment);
		return res;
	}
	
	@SuppressWarnings("unchecked")
	private void fillChangedComments(View v, JSONArray comments, JSONObject view2comment) {
		JSONArray commentids = new JSONArray();
		for (Comment c: v.getUncommittedComments()) {
			JSONObject comment = new JSONObject();
			comment.put("id", c.getMdid());
			comment.put("body", c.getDocumentation());
			comment.put("deleted", c.getDeleted());
			User u = c.getAuthor();
			if (u != null)
				comment.put("author", c.getAuthor().getUserName());
			comment.put("modified", Util.DATEFORMAT.format(c.getModified().toGregorianCalendar().getTime()));
			commentids.add(c.getMdid());
			comments.add(comment);
		}
		view2comment.put(v.getMdid(), commentids);
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
