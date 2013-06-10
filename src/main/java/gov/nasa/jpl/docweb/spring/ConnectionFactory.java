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
 */
package gov.nasa.jpl.docweb.spring;

import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;

/**
 * ConnectionFactories implement a strategy to create, initialize, cleanup and destroy Sesame's RepositoryConnections.
 */
public interface ConnectionFactory<C extends RepositoryConnection> {
    String DEFAULT_TYPE = "null";

    /**
     * @return The connections that should be used in the caller's context.
     * 
     * @throws
     */
    C getCurrentConnection();

    /**
     * @return The type of connection that is or will be used for {@link #getCurrentConnection()}. The default is null.
     *         ConnectionFactories that do not support different types of connections will allways return null.
     */
    String getCurrentConnectionType();

    /**
     * @param type. The type of connection that should be used for {@link #getCurrentConnection()} in this context.
     *        If set to null, the default type will be used.
     * 
     * @throws IllegalStateException when there already is a connection for this context, so the type is already determined,
     *         or when this method has been called previously for the current scope with another argument value.
     */
    void setCurrentConnectionType(String type);

    /**
     * Close and dispose of a connection if one is open. Effectively a call to getCurrentConnection()
     * after closeCurrentConnection() will return a new RepositoryConnection. Uncommited changes to
     * the underlying store will be lost.
     */
    void closeCurrentConnection();

    /**
     * Return the value factory of the underlying repository.
     */
    ValueFactory getValueFactory();

    /**
     * Called by a transaction manager on transaction start.
     */
    void setTransaction(SesameTransactionObject txObject);
}
