package gov.nasa.jpl.docweb.services;

import gov.nasa.jpl.docweb.concept.Comment;
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

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.RDFObject;
import org.springframework.stereotype.Service;

@Service
public class ViewService {

	/**
	 * 
	 * @param e {"mdid": mdid, "name": name, "documentation": blah, "type": View/...}
	 * @param oc
	 * @return
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 * @throws DatatypeConfigurationException
	 */
	public ModelElement updateOrCreateModelElement(JSONObject e, ObjectConnection oc, Set<String> merged) throws RepositoryException, QueryEvaluationException, DatatypeConfigurationException {
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
	
	public void updateModelElement(JSONObject e, ObjectConnection oc) throws RepositoryException, QueryEvaluationException {
		String id = (String)e.get("mdid");
		ModelElement res = null;
		try {
			res = oc.getObject(ModelElement.class, URI.DATA + id);
		} catch (ClassCastException ex) {
			return;
		}
		String name = null;
		if (res instanceof NamedElement)
			name = ((NamedElement)res).getName();
		String doc = res.getDocumentation();
		String dvalue = "";
		if (res instanceof Property) {
			dvalue = ((Property)res).getDefaultValue();
		}
		if ((e.containsKey("name") && name == null || e.containsKey("name") && !name.equals((String)e.get("name"))) ||
			(e.containsKey("documentation") && !doc.equals((String)e.get("documentation"))) ||
			(e.containsKey("dvalue") && dvalue == null || e.containsKey("dvalue") && !dvalue.equals((String)e.get("dvalue")))) {
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
			if (oldv instanceof NamedElement)
				((NamedElement)oldv).setName(name);
			oldv.setCommitted(res.getCommitted());
			oldv.setModified(res.getModified());
			oldv.setOldVersion(res.getOldVersion());
			if (oldv instanceof Property)
				((Property)oldv).setDefaultValue(((Property)res).getDefaultValue());
			res.setOldVersion(oldv);
			
			if (e.containsKey("name"))
				((NamedElement)res).setName((String)e.get("name"));
			if (e.containsKey("documentation"))
				res.setDocumentation((String)e.get("documentation"));
			if (e.containsKey("dvalue"))
				((Property)res).setDefaultValue((String)e.get("dvalue"));
			res.setCommitted(false);
			res.setMdid(id);
			try {
				res.setModified(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)GregorianCalendar.getInstance()));
			} catch (DatatypeConfigurationException e1) {
				e1.printStackTrace();
			}
		}
		if (res instanceof View && ((View)res).getContains().isEmpty()) {			
			RDFObject blah = oc.getObjectFactory().createObject();
			Paragraph p = oc.addDesignation(blah, Paragraph.class);
			p.addSource(res);
			p.setUseProperty("DOCUMENTATION");
			p.setIndex(0);
			((View)res).addContainedElement(p);
		}
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
	public View updateOrCreateView(JSONObject view, ObjectConnection oc, User u) throws RepositoryException, QueryEvaluationException, DatatypeConfigurationException {
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
	public NonContainerElement createContainedElement(JSONObject o, int index, ObjectConnection oc) throws RepositoryException, QueryEvaluationException {
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
	
	
	
	
	@SuppressWarnings("unchecked")
	public JSONObject getViewJSON(View v, boolean recurse) {
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
	public JSONObject getSingleViewJSON(View v, JSONObject elements) {
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
	public JSONArray getChangedElements(View v, boolean recurse) {
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
	
	
}
