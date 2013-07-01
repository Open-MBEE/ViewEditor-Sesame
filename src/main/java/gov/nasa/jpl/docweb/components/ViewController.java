package gov.nasa.jpl.docweb.components;

import gov.nasa.jpl.docweb.concept.Comment;
import gov.nasa.jpl.docweb.concept.DocumentView;
import gov.nasa.jpl.docweb.concept.ModelElement;
import gov.nasa.jpl.docweb.concept.NamedElement;
import gov.nasa.jpl.docweb.concept.NonContainerElement;
import gov.nasa.jpl.docweb.concept.Paragraph;
import gov.nasa.jpl.docweb.concept.Project;
import gov.nasa.jpl.docweb.concept.Property;
import gov.nasa.jpl.docweb.concept.Source;
import gov.nasa.jpl.docweb.concept.Table;
import gov.nasa.jpl.docweb.concept.Text;
import gov.nasa.jpl.docweb.concept.URI;
import gov.nasa.jpl.docweb.concept.User;
import gov.nasa.jpl.docweb.concept.Util;
import gov.nasa.jpl.docweb.concept.View;
import gov.nasa.jpl.docweb.resources.ViewResource;
import gov.nasa.jpl.docweb.services.CommentService;
import gov.nasa.jpl.docweb.services.ProjectService;
import gov.nasa.jpl.docweb.services.UserService;
import gov.nasa.jpl.docweb.services.ViewService;
import gov.nasa.jpl.docweb.spring.LocalConnectionFactory;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value="/ui/views")
public class ViewController {

	@Autowired
	private LocalConnectionFactory<ObjectConnection> connectionFactory;	
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CommentService commentService;
	
	@Autowired
	private ProjectService projectService;
	
	@Autowired
	private ViewService viewService;
	
	private static Logger log = Logger.getLogger(ViewController.class.getName());
	
	@Transactional(readOnly=true)
	@RequestMapping(value = "/{view}", method=RequestMethod.GET)
	public String getView(@PathVariable String view, Model model, Principal principal) throws RepositoryException, QueryEvaluationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		View v = oc.getObject(View.class, URI.DATA + view);
		Set<View> parentv = v.getParentViews();
		List<String> parents = new ArrayList<String>();
		for (View pv: parentv)
			parents.add(pv.getMdid());
		DocumentView parentDoc = null;
		if (v instanceof DocumentView)
			parentDoc = (DocumentView)v;
		else
			parentDoc = v.getParentDocument().iterator().next();
		Project project = parentDoc.getProject().iterator().next();
		model.addAttribute("projects", projectService.getProjects(oc));
		model.addAttribute("projectId", project.getMdid());
		model.addAttribute("viewParentIds", parents); //this is for opening the nav tree
		model.addAttribute("viewName", v.getName());
		model.addAttribute("viewId", view);
		model.addAttribute("viewDoc", v.getDocumentation());
		model.addAttribute("lastUser", v.getLastModifiedBy().getUserName());
		model.addAttribute("lastModified", v.getLastModified());
		model.addAttribute("currentUser", principal.getName());
		addViewDetail(v, model, oc);
		addViewComments(v, model);
		
		Map<String, Map<String, Object>> res = new HashMap<String, Map<String, Object>>();
		populateViewTree(parentDoc, res, new ArrayList<Integer>());
		model.addAttribute("documentId", parentDoc.getMdid());
		model.addAttribute("viewTree", res);
		return "view";
	}
	
	@Transactional
	@RequestMapping(value = "/{view}", method=RequestMethod.POST)
	@ResponseBody
	public String changeView(@PathVariable String view, @RequestBody String body, @RequestParam(value="type", required=false) String type) throws RepositoryException, ParseException, QueryEvaluationException, DatatypeConfigurationException {
		if (type != null && type.equals("instance")) {
			return body;
		}
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		String user = SecurityContextHolder.getContext().getAuthentication().getName();
		log.info("changing view " + view + ":\n" + body);
		JSONArray elements = (JSONArray)(new JSONParser()).parse(body);
		for (Object o: elements) {
			JSONObject jo = (JSONObject)o;
			viewService.updateModelElement(jo, oc);
		}
		View v = oc.getObject(View.class, URI.DATA + view);
		User u = userService.getOrCreateUser(user, oc);
		v.setLastModifiedBy(u);
		v.setLastModified(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)GregorianCalendar.getInstance()));
		return "ok";
	}
	
	@Transactional
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/{view}/comment", method=RequestMethod.POST)
	public String addComment(@PathVariable String view, @RequestParam(value = "body") String body, Principal principal) throws RepositoryException, QueryEvaluationException, DatatypeConfigurationException {
		String user = principal.getName();
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		log.info("adding comment for view " + view + ":\n" + body);
		View viewOb = oc.getObject(View.class, URI.DATA + view);
		JSONObject comment = new JSONObject();
		comment.put("body", body);
		comment.put("author", user);
		comment.put("modified", Util.DATEFORMAT.format(new Date()));
		comment.put("id", "comment" + UUID.randomUUID().toString().replace('-', '_'));
		Comment com = commentService.updateOrCreateComment(comment, oc);
		com.setCommitted(false);
		viewOb.addComment(com);
		return "redirect:/ui/views/" + view;
	}
	
	private void addViewDetail(View view, Model model, ObjectConnection oc) throws RepositoryException, QueryEvaluationException {
		List<Map<String, ? extends Object>> detail = new ArrayList<Map<String, ? extends Object>>();
		Set<String> seen = new HashSet<String>();
		for(NonContainerElement nce: view.getOrderedContainedElements()) {
			if (nce instanceof Paragraph) {
				Paragraph para = (Paragraph)nce;
				if (para.getSources().isEmpty())
					continue;
				Source source = para.getSources().iterator().next();
				Map<String, String> el = new HashMap<String, String>();
				if (source instanceof ModelElement) {
					String mdid = ((ModelElement)source).getMdid();
					el.put("mdid", mdid);
					String use = para.getUseProperty();
					if (use.equals("DOCUMENTATION")) {
						el.put("type", "doc");
						el.put("documentation", ((ModelElement)source).getDocumentation());
						if (seen.contains(mdid + "-doc"))
							el.put("edit", "false");
						else
							el.put("edit", "true");
						seen.add(mdid + "-doc");
					} else if (use.equals("NAME") && source instanceof NamedElement) {
						el.put("type", "name");
						el.put("name", ((NamedElement)source).getName());
						if (seen.contains(mdid + "-name"))
							el.put("edit", "false");
						else
							el.put("edit", "true");
						seen.add(mdid + "-name");
					}
				} else if (source instanceof Text) {
					el.put("type", "text");
					el.put("text", ((Text)source).getText());
				}
				detail.add(el);
			} else if (nce instanceof Table) {
				String body = ((Table)nce).getBody();
				String header = ((Table)nce).getHeader();
				String style = ((Table)nce).getStyle();
				JSONArray bodyo = (JSONArray)JSONValue.parse(body);
				JSONArray headero = (JSONArray)JSONValue.parse(header);
				Map<String, Object> table = new HashMap<String, Object>();
				table.put("title", ((Table)nce).getTitle());
				table.put("type", "table");
				table.put("style", style);
				List<List<Map<String, String>>> headers = new ArrayList<List<Map<String, String>>>();
				List<List<Map<String, String>>> bodys = new ArrayList<List<Map<String, String>>>();
				addTableRows(headers, headero, oc, seen);
				addTableRows(bodys, bodyo, oc, seen);
				table.put("body", bodys);
				table.put("header", headers);
				detail.add(table);
			}
		}
		model.addAttribute("viewDetail", detail);
	}
	
	@SuppressWarnings("unchecked") // for the List<JSONObject cast
	private void addTableRows(List<List<Map<String, String>>> bodys, JSONArray bodyo, ObjectConnection oc, Set<String> seen) throws RepositoryException, QueryEvaluationException {
		for (Object row: bodyo) {
			List<Map<String, String>> currow = new ArrayList<Map<String, String>>();
			bodys.add(currow);
			for (JSONObject entry: (List<JSONObject>)row) {
				String source = (String)entry.get("source");
				Map<String, String> el = new HashMap<String, String>();
				if (source.equals("text")) {
					el.put("type", "text");
					el.put("text", (String)entry.get("text"));
				} else {
					String use = (String)entry.get("useProperty");
					ModelElement e = oc.getObject(ModelElement.class, URI.DATA + source);
					String mdid = e.getMdid();
					el.put("mdid", mdid);
					if (use.equals("DOCUMENTATION")) {
						el.put("type", "doc");
						el.put("documentation", e.getDocumentation());
						if (seen.contains(mdid + "-doc"))
							el.put("edit", "false");
						else
							el.put("edit", "true");
						seen.add(mdid + "-doc");
					} else if (use.equals("NAME") && e instanceof NamedElement) {
						el.put("type", "name");
						el.put("name", ((NamedElement)e).getName());
						if (seen.contains(mdid + "-name"))
							el.put("edit", "false");
						else
							el.put("edit", "true");
						seen.add(mdid + "-name");
					} else if (use.equals("DVALUE") && e instanceof Property) {
						el.put("type", "dvalue");
						el.put("dvalue", ((Property)e).getDefaultValue());
						if (seen.contains(mdid + "-dvalue"))
							el.put("edit", "false");
						else
							el.put("edit", "true");
						seen.add(mdid + "-dvalue");
					}
				}
				if (entry.containsKey("colspan"))
					el.put("colspan", (String)entry.get("colspan"));
				if (entry.containsKey("rowspan"))
					el.put("rowspan", (String)entry.get("rowspan"));
				currow.add(el);
			}
			
		}
	}
	
	private void addViewComments(View view, Model model) {
		List<Map<String, String>> comments = new ArrayList<Map<String, String>>();
		for (Comment c: view.getOrderedComments()) {
			Map<String, String> comment = new HashMap<String, String>();
			comment.put("author", c.getAuthor().getUserName());
			comment.put("body", c.getDocumentation());
			comment.put("timestamp", Util.DATEFORMAT.format(c.getModified().toGregorianCalendar().getTime()));
			comment.put("id", c.getMdid());
			comments.add(0, comment);
		}
		model.addAttribute("comments", comments);
	}
	
	private boolean populateViewTree(View dv, Map<String, Map<String, Object>> res, List<Integer> number) {
		Map<String, Object> info = new HashMap<String, Object>();
		info.put("name", dv.getName());
		info.put("mdid", dv.getMdid());
		boolean increment = false;
		Boolean nosection = dv.getNoSection();
		if (nosection != null && !nosection) {
			info.put("section", join(number, ".") + " ");
			increment = true;
		} else
			info.put("section", "");
		List<String> ordered = new ArrayList<String>();
		int c = 1;
		for (View view: dv.getOrderedChildrenViews()) {
			ordered.add(view.getMdid());
			number.add(c);
			if (populateViewTree(view, res, number)) {
				c++;
			}
			number.remove(number.size()-1);
		}
		info.put("children", ordered);
		res.put(dv.getMdid(), info);
		return increment;
	}
	
	private String join(Collection<?> s, String delimiter) {
    	StringBuilder builder = new StringBuilder();
    	Iterator<?> iter = s.iterator();
    	while (iter.hasNext()) {
    		builder.append(iter.next());
    		if (!iter.hasNext())
    			break;
    		builder.append(delimiter);
    	}
    	return builder.toString();
    }
}
