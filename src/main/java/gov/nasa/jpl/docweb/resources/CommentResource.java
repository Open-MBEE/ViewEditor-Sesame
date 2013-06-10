package gov.nasa.jpl.docweb.resources;

import gov.nasa.jpl.docweb.concept.Comment;
import gov.nasa.jpl.docweb.concept.URI;
import gov.nasa.jpl.docweb.concept.View;
import gov.nasa.jpl.docweb.spring.LocalConnectionFactory;

import java.util.Set;
import java.util.logging.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value="/rest/comments")
public class CommentResource {

	@Autowired
	private LocalConnectionFactory<ObjectConnection> connectionFactory;
	
	private static Logger log = Logger.getLogger(CommentResource.class.getName());
	
	/**
	 * <p>This is to indicate which comments should be mark committed, if user imported comments from web, change the comment*** ids to mdids
	 * @param body {oldid: newid, ...}
	 * @return
	 * @throws RepositoryException
	 * @throws ParseException
	 * @throws QueryEvaluationException
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	@RequestMapping(value="/committed", method=RequestMethod.POST)
	public @ResponseBody String commit(@RequestBody String body) throws RepositoryException, ParseException, QueryEvaluationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		log.info("committed comments: " + body);
		JSONObject elements = (JSONObject)(new JSONParser()).parse(body);
		for (String o: (Set<String>)elements.keySet()) {
			try {
				Comment e = oc.getObject(Comment.class, URI.DATA + o);
				if (!o.startsWith("comment") || o.equals((String)elements.get(o))) {
					e.setCommitted(true);
					continue;
				}
				String newid = (String)elements.get(o);
				Comment newc = oc.getObjectFactory().createObject(URI.DATA + newid, Comment.class);
				//Comment newc = oc.addDesignation(oc.getObjectFactory().createObject(URI.DATA + newid, Comment.class), Comment.class);
				newc.setAuthor(e.getAuthor());
				newc.setCommitted(true);
				newc.setDocumentation(e.getDocumentation());
				newc.setModified(e.getModified());
				newc.setMdid(newid);
				newc.setDeleted(false);
				oc.addDesignation(newc, Comment.class);
				e.setCommitted(true);
				e.setDeleted(true);
				for (View parent: e.getCommentedViews()) {
					log.info("changing view " + parent.getMdid() + "'s comment " + o + " to " + newid);
					parent.addComment(newc);
					parent.removeComment(e);
				}
				newc.setOldVersion(e);
			} catch (ClassCastException e) {
				log.info("cannot find comment: " + o);
			}
		}
		return "ok";//Response.status(200).build();
	}

	@Transactional
	@RequestMapping(value="/{commentid}", method=RequestMethod.DELETE)
	public @ResponseBody String removeComment(@PathVariable("commentid") String commentid) throws RepositoryException, QueryEvaluationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		log.info("deleting comment " + commentid);
		try {
			Comment e = oc.getObject(Comment.class, URI.DATA + commentid);
			e.setDeleted(true);
			e.setCommitted(false);
		} catch (ClassCastException e) {
			log.info("comment " + commentid + " not found! will not set delete");
		}
		return commentid;//Response.status(200).build();
	}
	
	@Transactional
	@RequestMapping(value="/{commentid}", method=RequestMethod.POST)
	public @ResponseBody String postComment(@PathVariable("commentid") String commentid, @RequestBody String body) throws RepositoryException, QueryEvaluationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();;
		log.info("posting comment " + commentid);
		try {
			Comment e = oc.getObject(Comment.class, URI.DATA + commentid);
			e.setDocumentation(body);
			e.setCommitted(false);
		} catch (ClassCastException e) {
			log.info("comment " + commentid + " not found! will not set delete");
		}
		return commentid;//Response.status(200).build();
	}
}
