/*
 * Copyright 2010 by TalkingTrends (Amsterdam, The Netherlands)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensahara.com/licenses/apache-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * Taken from useekm: https://dev.opensahara.com/projects/useekm
 * 
 * Modified by CYL
 */
package gov.nasa.jpl.docweb.spring;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A ConnectionFactory that implements a ThreadLocal strategy for creating RepositoryConnections.
 *
 * @see SesameTransactionManager
 */
@Component
public class LocalConnectionFactory<C extends RepositoryConnection> implements ConnectionFactory<C>, DisposableBean {
    private static final Logger LOG = LoggerFactory.getLogger(LocalConnectionFactory.class);

    // settings:
    @Autowired
    private RepositoryProvider repositoryProvider;

    // state:
    private Repository repository;
    private final ThreadLocal<String> connectionType = new ThreadLocal<String>();
    private final ThreadLocal<C> connections = new ThreadLocal<C>();
    private final ThreadLocal<SesameTransactionObject> txConnections = new ThreadLocal<SesameTransactionObject>();
    private int unclosedConnections;// = 0;

    protected LocalConnectionFactory() {
    }

    /**
     * Return a {@link RepositoryConnection} for the current thread. If the call is made outside the context of
     * a {@link SesameTransactionManager}, it should be closed with {@link #closeCurrentConnection()}.
     * 
     * @see SesameTransactionManager
     */
    @Override
    public C getCurrentConnection() {
        return getCurrentConnection(true);
    }

    @SuppressWarnings("unchecked")
    //Spring bean not generic
    public C getCurrentConnection(boolean createIfNew) {
        C result = connections.get();
        try {
            if (result == null && createIfNew) {
                result = (C)getRepository().getConnection();
                connections.set(result);
                synchronized (this) {
                    ++unclosedConnections;
                }
            }
            // [CYL] always set autocommit to false
            updateTx(result);
            return result;
        } catch (RepositoryException e) {
            throw ExceptionConverter.convertException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentConnectionType() {
        String result = connectionType.get();
        return ConnectionFactory.DEFAULT_TYPE.equals(result) ? null : result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentConnectionType(String type) {
        if (type == null)
            type = ConnectionFactory.DEFAULT_TYPE;
        String prevType = connectionType.get();
        if (prevType != null && !prevType.equals(type))
            throw new IllegalStateException("Trying to change connectionType from " + prevType + " to " + type);
        if (prevType == null && getCurrentConnection(false) != null)
            throw new IllegalStateException("Trying to change connectionType to " + type + ", but a connection is already active in the scope.");
        if (prevType == null)
            connectionType.set(type);
    }

    /**
     * Close the currently active {@link RepositoryConnection} for this thread. If no {@link RepositoryConnection}
     * was opened yet, the call has no effect.
     */
    @Override
    public void closeCurrentConnection() {
        RepositoryConnection toClose;
        synchronized (this) {
            toClose = connections.get();
            if (toClose != null && unclosedConnections > 0)
                --unclosedConnections;
            connections.remove();
            txConnections.remove();
            connectionType.remove();
        }
        if (toClose != null)
            close(toClose);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueFactory getValueFactory() {
        return getRepository().getValueFactory();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTransaction(SesameTransactionObject txObject) {
        txConnections.set(txObject);
    }

    public void setRepositoryProvider(RepositoryProvider sailProvider) {
        this.repositoryProvider = sailProvider;
    }

    public RepositoryProvider getRepositoryProvider() {
        return this.repositoryProvider;
    }

    private Repository getRepository() {
        synchronized (this) {
            if (repository == null) {
                repository = repositoryProvider.createRepository(this);
                try {
                    repository.initialize();
                } catch (RepositoryException e) {
                    repository = null;
                    throw ExceptionConverter.convertException(e);
                }
            }
        }
        return repository;
    }

    @Override
    public void destroy() {
        synchronized (this) {
            try {
                if (repository != null)
                    repository.shutDown();
            } catch (RepositoryException e) {
                LOG.error("Could not close sesame repository", e);
                // no re-throw, spring will just ignore a re-throw anyway.
            } finally {
                repository = null;
                repositoryProvider.cleanup(this);
            }
            if (unclosedConnections > 0)
                LOG.error("Not all RepositoryConnections are closed, count of connections still open: {}",
                    unclosedConnections);
        }
    }

    private void updateTx(RepositoryConnection conn) throws RepositoryException {
        SesameTransactionObject tx = txConnections.get();
        if (tx != null) {
            tx.setConnection(conn);
            conn.setAutoCommit(false);
        }
    }

    private void close(RepositoryConnection conn) {
        try {
            conn.close();
        } catch (RepositoryException e) {
            throw ExceptionConverter.convertException(e);
        }
    }
}
