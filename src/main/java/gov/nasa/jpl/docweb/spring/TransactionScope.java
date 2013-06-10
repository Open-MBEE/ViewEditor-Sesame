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

import org.springframework.transaction.annotation.Transactional;

import gov.nasa.jpl.docweb.spring.ExceptionConverter.Function;

/**
 * Helper class, see {@link #asTransactionInner(Function)}
 */
class TransactionScope {
    /**
     * Executes the provided function wrapped in a transaction from the default
     * {@link Transactional} scope.
     */
    @Transactional
    protected final <T> T asTransactionInner(Function<T> function) {
        return ExceptionConverter.convert(function);
    }
}
