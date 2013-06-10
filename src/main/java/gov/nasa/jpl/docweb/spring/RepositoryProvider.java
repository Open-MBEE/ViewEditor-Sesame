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

import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

/**
 * Strategy for repository creation.
 * 
 * @see SailRepositoryProvider
 * @see ObjectRepositoryProvider
 */
public interface RepositoryProvider {
    /**
     * Create or return an uninitialized repository according to the provider's rules.
     * Should be called only once in the lifetime of the application, because it is not specified wether a new
     * call returns the same or a new {@link Repository}.
     * <p>
     * The application should call {@link #cleanup()} when it is done with the Repository.
     * 
     * @param connectionFactory The connectionFactory that manages connections for this RepositoryProvider.
     */
    Repository createRepository(ConnectionFactory<? extends RepositoryConnection> connectionFactory);

    /**
     * Does additional cleanup. {@link Repository#shutDown()} should be called before calling cleanup.
     */
    void cleanup(ConnectionFactory<? extends RepositoryConnection> connectionFactory);
}
