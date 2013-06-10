/*
 * Copyright 2011 by TalkingTrends (Amsterdam, The Netherlands)
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

import org.apache.commons.lang.Validate;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.transaction.TransactionSystemException;

class SesameTransactionObject {
    private RepositoryConnection connection;
    private boolean existing;
    private boolean rollbackOnly;// = false;

    SesameTransactionObject(boolean isExisting) {
        this.existing = isExisting;
    }

    void setExisting(boolean e) {
        this.existing = e;
    }

    boolean isExisting() {
        return existing;
    }

    void setRollbackOnly(boolean rbo) {
        this.rollbackOnly = rbo;
    }

    boolean isRollbackOnly() {
        return rollbackOnly;
    }

    RepositoryConnection getConnection() {
        return connection;
    }

    void setConnection(RepositoryConnection connection) throws RepositoryException {
        Validate.notNull(connection);
        if (this.connection != null && this.connection != connection)
            throw new TransactionSystemException("Change of connection detected in @Transactional managed code");
        if (this.connection == null)
            connection.setAutoCommit(false);
        this.connection = connection;
    }
}
