/*
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.microprofile.rest.client.ext;

/**
 * Implementations of this interface can intercept asynchronous method
 * invocations.  The MP Rest Client implementation runtime will obtain instances
 * of this interface by invoking the <code>newInterceptor</code> method of all
 * registered <code>AsyncInvocationInterceptorFactory</code> providers.
 *
 * The MP Rest Client implementation runtime will invoke the <code>pre</code>
 * method on the main thread prior to returning execution back to the calling
 * method of the client interface.  The runtime will invoke the
 * <code>prepareContext</code> method on the invocation thread before the client
 * request is sent.  The <code>prepareContext</code> method should always be
 * invoked before the <code>applyContext</code> method is invoked, but due to
 * the nature of multithreading, it is possible that <code>applyContext</code>
 * method may be invoked before the <code>prepareContext</code> method has
 * completed.  Care should be taken when implementing this interface to avoid
 * race conditions and deadlocks.
 *
 * Note that the order in which instances of the
 * <code>AsyncInvocationInterceptor</code> are invoked are determined by the
 * priority of the <code>AsyncInvocationInterceptorFactory</code> provider.
 *
 * Note that the main and secondary threads handling the request/response may
 * be the same. It depends on how the implementation chooses to implement the
 * asynchronous handling.
 *
 * @since 1.1
 */
public interface AsyncInvocationInterceptor {

    /**
     * This method will be invoked by the MP Rest Client runtime on the "main"
     * thread (i.e. the thread calling the async Rest Client interface method)
     * prior to returning control to the calling method.
     */
    void prepareContext();

    /**
     * This method will be invoked by the MP Rest Client runtime on the "async"
     * thread (i.e. the thread used to actually invoke the remote service and
     * wait for the response) prior to sending the request.
     */
    void applyContext();

    /**
     * This method will be invoked by the MP Rest Client runtime on the "async"
     * thread (i.e. the thread used to actually invoke the remote service and
     * wait for the response) after all providers on the inbound response flow
     * have been invoked.
     *
     * @since 1.2
     */
     void removeContext();
}
