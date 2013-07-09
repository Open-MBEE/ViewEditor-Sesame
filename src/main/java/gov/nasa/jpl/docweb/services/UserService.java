package gov.nasa.jpl.docweb.services;

import gov.nasa.jpl.docweb.concept.URI;
import gov.nasa.jpl.docweb.concept.User;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	public User getOrCreateUser(String username, ObjectConnection oc) throws RepositoryException, QueryEvaluationException {
		User u = null;
		try {
			u = oc.getObject(User.class, URI.USER + username);
		} catch (ClassCastException e) {
			u = oc.addDesignation(oc.getObjectFactory().createObject(URI.USER + username, User.class), User.class);
			u.setUserName(username);
		}
		return u;
	}
	
}
