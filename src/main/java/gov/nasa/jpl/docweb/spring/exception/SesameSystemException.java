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
package gov.nasa.jpl.docweb.spring.exception;

import org.openrdf.OpenRDFException;
import org.springframework.dao.UncategorizedDataAccessException;

import gov.nasa.jpl.docweb.spring.ExceptionConverter;


/**
 * Uncategorized runtime exception wrapper for exceptions thrown by Sesame.
 *
 * @see ExceptionConverter#convertException(String, OpenRDFException)
 */
public class SesameSystemException extends UncategorizedDataAccessException {
    private static final long serialVersionUID = 1L;

    public SesameSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
