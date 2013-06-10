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

import org.openrdf.OpenRDFException;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.springframework.dao.DataAccessException;

import gov.nasa.jpl.docweb.spring.exception.SesameMalformedQueryException;
import gov.nasa.jpl.docweb.spring.exception.SesameSystemException;

/**
 * Converts (by wrapping) exceptions thrown by Sesame into unchecked exceptions that
 * fit in the {@link DataAccessException} hierarchy used in the Spring framework.
 */
public final class ExceptionConverter {
    public interface Function<T> {
        T execute() throws OpenRDFException;
        RepositoryConnection conn() throws RepositoryException;
    }

    public static abstract class AbstractFunction<T, C extends RepositoryConnection> implements Function<T> {
        private final C conn;

        protected AbstractFunction(C connection) {
            conn = connection;
        }

        @Override public C conn() {
            return conn;
        }

        public ValueFactory vf() {
            return conn.getValueFactory();
        }
    }

    public static <T> T convert(Function<T> function) {
        try {
            return function.execute();
        } catch (OpenRDFException e) {
            throw convertException(e);
        }
    }
    
    private static TransactionScope txScope;

//  CYL:removed  @SuppressWarnings("PMD.NonThreadSafeSingleton") // singleton has no state and may be overwritten without side-effects
    public static <T> T asTransaction(Function<T> function) {
        //Transactional annotation on static methods does not work, additionally
        // this class should not be runtime dependent on @Transactional (this method is
        // obviously runtime dependent):
        if (txScope == null)
            txScope = newTransactionScope(); // Note: no sync needed since scope has no state
        return txScope.asTransactionInner(function);
    }

    private static TransactionScope newTransactionScope() {
        return new TransactionScope();
    }

    /**
     * @see #convertException(String, OpenRDFException)
     */
    public static DataAccessException convertException(OpenRDFException e) throws DataAccessException {
        throw convertException(e.getMessage(), e);
    }

    /**
     * Converts an Sesame {@link OpenRDFException} to a Spring {@link DataAccessException} (or subclass thereof), and
     * throws the converted exception.
     * @param e Exception to convert
     * @param message Description of the cause of the problem. See {@link Exception#getMessage()}.
     * @return The function does not return, it always throws a {@link DataAccessException}. The return value in its
     *         interface is added to make it possible to write {@code throw convertException(mess, e)} which makes it
     *         clear to the compiler that the flow of control is interrupted.
     * @throws DataAccessException
     */
    public static DataAccessException convertException(String message, OpenRDFException e) throws DataAccessException {
        if (e instanceof MalformedQueryException)
            throw new SesameMalformedQueryException(message, e);
        throw new SesameSystemException(message, e);
    }
}
