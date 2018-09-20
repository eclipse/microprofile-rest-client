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
 * This is a provider interface intended for intercepting asynchronous method
 * invocations.  Registered implementations of this interface will be invoked
 * to build a new <code>AsyncInvocationInterceptor</code>.  This interceptor
 * would then be invoked before and after swapping threads for an asynchronous
 * client invocation.
 *
 * The priority of this provider determines when the factory's
 * <code>newInterceptor</code> method will be invoked - but also the
 * <code>AsyncInvocationInterceptor</code>'s lifecycle methods as well.  Like
 * other providers, the priority is ascending (where a provider with priority 1
 * will execute before a provider of priority 2), however, the interceptors'
 * <code>applyContext</code> methods will be invoked in descending order.  The
 * priority of the provider can be specified using the
 * <code>javax.annotation.Priority</code> annotation or when registering the
 * provider using the <code>RestClientBuilder</code>.
 *
 * The timing of when providers of this interface is invoked relative to other
 * providers (such as filters, entity interceptors, etc.) is undefined.
 * Implementations of this or the <code>AsyncInvocationInterceptor</code>
 * interface should not rely on the order of other providers, as this could
 * change between different implementations of the MP Rest Client.
 * @since 1.1
 */
public interface AsyncInvocationInterceptorFactory {

    /**
     * Implementations of this method should return an implementation of the
     * <code>AsyncInvocationInterceptor</code> interface.  The MP Rest Client
     * implementation runtime will invoke this method, and then invoke the
     * <code>prepareContext</code> and <code>applyContext</code> methods of the
     * returned interceptor when performing an asynchronous method invocation.
     * Null return values will be ignored.
     *
     * @return Non-null instance of <code>AsyncInvocationInterceptor</code>
     */
    AsyncInvocationInterceptor newInterceptor();
}
