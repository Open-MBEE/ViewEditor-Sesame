package gov.nasa.jpl.docweb.services;

import gov.nasa.jpl.docweb.concept.Comment;
import gov.nasa.jpl.docweb.concept.URI;
import gov.nasa.jpl.docweb.concept.User;
import gov.nasa.jpl.docweb.concept.Util;
import gov.nasa.jpl.docweb.concept.View;

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

	@Autowired
	private UserService userService;
	
	/**
	 * @param commentob {"id": mdid, "body": body, "author": user, "modified": datetime}
	 * @param oc
	 * @return
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 * @throws DatatypeConfigurationException
	 */
	public Comment updateOrCreateComment(JSONObject commentob, ObjectConnection oc) throws RepositoryException, QueryEvaluationException, DatatypeConfigurationException {
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
			u = userService.getOrCreateUser(user, oc);
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
	
	@SuppressWarnings("unchecked")
	public JSONObject getChangedComments(View v, boolean recurse) {
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
	public void fillChangedComments(View v, JSONArray comments, JSONObject view2comment) {
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
}
