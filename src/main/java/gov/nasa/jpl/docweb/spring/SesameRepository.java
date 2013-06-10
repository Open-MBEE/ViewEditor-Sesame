package gov.nasa.jpl.docweb.spring;

import java.io.File;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.repository.manager.RepositoryProvider;
import org.openrdf.repository.object.ObjectConnection;
import org.openrdf.repository.object.ObjectRepository;
import org.openrdf.repository.object.config.ObjectRepositoryFactory;

/**
 * Deprecated in favor of {@link SesameRepositoryProvider} used by {@link SesameTransactionManager}
 */
@Deprecated
public class SesameRepository {
	
	private ObjectRepository or;
	
	@PreDestroy
	public void shutdown() {
		System.out.println("Closing sesame repository...");
		if (or != null && or.isInitialized())
			try {
				or.shutDown();
				System.out.println("Sesame repository shutdown.");
			} catch (RepositoryException e) {
				e.printStackTrace();
			}
	}

	@PostConstruct
	public void startup() {
		System.out.println("Creating sesame repository...");		 
		try {
			String loc = System.getProperty("sesame.rep", "/data/www/sesame_data/openrdf-sesame");
			RepositoryManager rm = RepositoryProvider.getRepositoryManager(new File(loc));
			Repository repo = rm.getRepository("editor");
			if (!repo.isInitialized())
				repo.initialize();
			or = new ObjectRepositoryFactory().createRepository(repo);
			System.out.println("Created repository");
		} catch (RepositoryConfigException e) {
			e.printStackTrace();
		} catch (RepositoryException e) {
			e.printStackTrace();
		} 
	}
	
	public ObjectConnection getConnection() throws RepositoryException {
		return or.getConnection();
	}
}
