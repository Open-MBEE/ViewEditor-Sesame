package gov.nasa.jpl.docweb.spring;

import java.io.File;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import gov.nasa.jpl.docweb.spring.ConnectionFactory;
import gov.nasa.jpl.docweb.spring.RepositoryProvider;

@Component
public class SesameRepositoryProvider implements RepositoryProvider, InitializingBean {
	private ObjectRepository or = null;
	
	@Override
	public void cleanup(ConnectionFactory<? extends RepositoryConnection> arg0) {
		System.out.println("SRP: Closing sesame repository...");
		if (or != null) { // && or.isInitialized()) {
			try {
				or.shutDown();
				System.out.println("SRP: Sesame repository shutdown.");
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
		} 
	}

	@Override
	public Repository createRepository(
			ConnectionFactory<? extends RepositoryConnection> arg0) {
		String loc = System.getProperty("sesame.rep", "/data/www/sesame_data/openrdf-sesame");
		
		if (or != null) {
			return or;
		}
		
		try {
			RepositoryManager rm = org.openrdf.repository.manager.RepositoryProvider.getRepositoryManager(new File(loc));
			Repository repo = rm.getRepository("editor");
			if (!repo.isInitialized()) {
				repo.initialize();
			}
			or = new ObjectRepositoryFactory().createRepository(repo);
			System.out.println("SRP: Created repository");
		} catch (RepositoryConfigException e) {
			System.out.println("ERROR: could not load Sesame store at: " + loc);
			e.printStackTrace();
		} catch (RepositoryException e) {
			System.out.println("ERROR: could not load Sesame store at: " + loc);
			e.printStackTrace();
		} 
		
		return or;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// do nothing
	}

}
