package gov.nasa.jpl.docweb.spring;

/**
 * Modified from useekm: https://dev.opensahara.com/projects/useekm
 */

import org.apache.commons.lang.Validate;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jca.support.LocalConnectionFactoryBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * A {@link PlatformTransactionManager} for Sesame {@link RepositoryConnection}. This manager requires that a
 * participating {@link RepositoryConnection} is completely managed by the SesameTransactionManager. Exceptions will be
 * thrown if an already opened connection for the executing thread is returned by the {@link ConnectionFactory}.
 * <p>
 * The SesameTransactionManager will close the the {@link RepositoryConnection} when the transaction scope is ended (committed or rolled back).
 * <p>
 * Save points, suspension of transactions, custom isolation levels, and timeouts are not supported.
 * </p>
 * 
 * @see LocalConnectionFactoryBean
 */
@Component
public class SesameTransactionManager extends AbstractPlatformTransactionManager implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(SesameTransactionManager.class);
    private static final long serialVersionUID = 1L;
    @Autowired
    private ConnectionFactory<RepositoryConnection> connectionFactory;
    private final ThreadLocal<SesameTransactionObject> txConnections = new ThreadLocal<SesameTransactionObject>();

    //Protected constructor for spring
    protected SesameTransactionManager() {}

    /**
     * Set the ConnectionFactory that this instance should manage transactions for.
     */
    public void setConnectionFactory(ConnectionFactory<RepositoryConnection> connectionFactory) {
    	System.out.println("SesameTransactionManager: setting connection factory");
        this.connectionFactory = connectionFactory;
    }

    /**
     * @see #setConnectionFactory(ConnectionFactory)
     */
    final public ConnectionFactory<RepositoryConnection> getConnectionFactory() {
        return this.connectionFactory;
    }

    public LocalConnectionFactory<RepositoryConnection> getLocalConnectionFactory() {
        return (LocalConnectionFactory<RepositoryConnection>)this.connectionFactory;
    }

    @Override public void afterPropertiesSet() {
        validate();
    }

    private void validate() {
        Validate.notNull(getConnectionFactory(), "Property 'connectionFactory' is required");
        Validate.isTrue(!isNestedTransactionAllowed(), "SesameTransactionManger does not support nested transactions");
    }

    @Override
    public Object doGetTransaction() {
        SesameTransactionObject result = txConnections.get();
        RepositoryConnection current = getLocalConnectionFactory().getCurrentConnection(false);
        if (result == null && current != null)
            throw new TransactionSystemException("An unmanaged connection does already exist");
        else if (result == null) {
            result = new SesameTransactionObject(false);
            txConnections.set(result);
            getLocalConnectionFactory().setTransaction(result);
        } else {
            if (result.getConnection() != current)
                throw new TransactionSystemException("Do not switch connections during managed transactions");
            try {
                if (current != null && current.isAutoCommit())
                    throw new TransactionSystemException("Do not call setAutoCommit in managed transactions");
            } catch (RepositoryException e) {
                throw ExceptionConverter.convertException(e);
            }
            result.setExisting(true);
        }
        return result;
    }

    @Override
    public boolean isExistingTransaction(Object transaction) {
        return ((SesameTransactionObject)transaction).isExisting();
    }

    @Override
    public void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        ((SesameTransactionObject)status.getTransaction()).setRollbackOnly(true);
    }

    @Override
    public void doBegin(Object transaction, TransactionDefinition definition) {
        int timeout = determineTimeout(definition);
        if (timeout != TransactionDefinition.TIMEOUT_DEFAULT)
            LOG.warn("Timeout {} for transaction {} ignored", timeout, definition.getName());
    }

    @Override
    public void doCommit(DefaultTransactionStatus status) {
        boolean commit = !((SesameTransactionObject)status.getTransaction()).isRollbackOnly();
        doEndTransaction(commit);
    }

    @Override
    public void doRollback(DefaultTransactionStatus status) {
        doEndTransaction(false);
    }

    private void doEndTransaction(boolean commit) {
        try {
            RepositoryConnection connection = getLocalConnectionFactory().getCurrentConnection(false);
            if (connection != null) {
                if (connection.isAutoCommit())
                    throw new TransactionSystemException("Do not mix calls to setAutoCommit with @Transactional");
                else if (commit)
                    connection.commit();
                else
                    connection.rollback();
            }
        } catch (RepositoryException e) {
            throw ExceptionConverter.convertException(e);
        }
    }

    @Override
    public void doCleanupAfterCompletion(Object transaction) {
        try {
            getConnectionFactory().closeCurrentConnection();
        } finally {
            txConnections.remove();
        }
    }
}
