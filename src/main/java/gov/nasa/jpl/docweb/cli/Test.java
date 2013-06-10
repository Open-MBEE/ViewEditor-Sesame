package gov.nasa.jpl.docweb.cli;

import gov.nasa.jpl.docweb.concept.Comment;
import gov.nasa.jpl.docweb.concept.DocumentView;
import gov.nasa.jpl.docweb.concept.ModelElement;
import gov.nasa.jpl.docweb.concept.Project;
import gov.nasa.jpl.docweb.concept.URI;
import gov.nasa.jpl.docweb.concept.User;
import gov.nasa.jpl.docweb.concept.View;
import gov.nasa.jpl.docweb.resources.ViewResource;

import java.io.File;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import javax.xml.datatype.DatatypeFactory;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.RepositoryProvider;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.openrdf.result.Result;

public class Test {

	/**
	 * @param args
	 * @throws RepositoryException 
	 */
	
	public static void initialize(ObjectConnection oc) throws RepositoryException {
		
		Comment c1 = oc.getObjectFactory().createObject(URI.DATA + "comment1", Comment.class);
		c1.setDocumentation("asdofm sommcnet");
		oc.addDesignation(c1, Comment.class);
		
		View viewA = oc.getObjectFactory().createObject(URI.DATA + "viewA", View.class);
		viewA.setName("viewA");
		viewA.setDocumentation("hahahhaah");
		oc.addDesignation(viewA, View.class);
		
		View viewB = oc.addDesignation(oc.getObjectFactory().createObject(), View.class);
		viewB.setName("viewB");
		
		View viewC = oc.getObjectFactory().createObject(URI.DATA + "viewC", View.class);
		viewC.setName("viewC");
		oc.addDesignation(viewC, View.class);

		viewA.addComment(c1);
		viewA.addChildView(viewB);
		viewA.addChildView(viewC);
		
		DocumentView dv = oc.addDesignation(oc.getObjectFactory().createObject(URI.DATA + "document1"), DocumentView.class);
		dv.setName("document1");
		dv.setDocumentation("docuemtn documentation");
		dv.addChildView(viewA);
	}
	
	public static void test(ObjectConnection oc) throws RepositoryException, QueryEvaluationException{
		try {
			View testView = oc.getObject(View.class, URI.DATA + "_2239482_234234_22341");
		} catch (ClassCastException e) {
			DocumentView dv = oc.addDesignation(oc.getObjectFactory().createObject(URI.DATA + "_2239482_234234_22341", DocumentView.class), DocumentView.class);
			dv.setMdid("_adsfsdf");
			dv.setName("testing");
			dv.setCommitted(true);
		}
	
	}
	
	public static void main(String[] args) {
		Repository repo;
		ObjectRepository op = null;
		ObjectConnection oc = null;
		try {
			
			RepositoryManager rm = RepositoryProvider.getRepositoryManager(new File("/Users/dlam/Documents/sesame/OpenRDF Sesame"));
			repo = rm.getRepository("editor");
			//repo = new HTTPRepository("http://localhost:8080/openrdf-sesame", "test");
			if (!repo.isInitialized())
				repo.initialize();
			op = new ObjectRepositoryFactory().createRepository(repo);
			
			oc = op.getConnection();
			oc.setAutoCommit(false);
			User u = ViewResource.getOrCreateUser("blah", oc);
			for (User user: oc.getObjects(User.class).asList()) {
				System.out.println(user.getUserName());
			}
			oc.commit();
		} catch (RepositoryConfigException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (oc != null)
				try {
					oc.rollback();
					oc.close();
				} catch (RepositoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			if (op != null)
				try {
					op.shutDown();
				} catch (RepositoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}

}
