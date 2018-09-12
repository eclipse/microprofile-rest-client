/*
 *******************************************************************************
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.eclipse.microprofile.rest.client.spi;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

/**
 * Implementations of this interface will be notified when new RestClientBuilder
 * instances are being constructed.  This will allow implementations to register
 * providers on the RestClientBuilder, and is intended for global providers.
 * For example, a MicroProfile OpenTracing implementation might want to register
 * a ClientRequestFilter to initiate tracing.
 *
 * In order for the RestClientBuilder to call implementations of this interface,
 * the implementation must be specified such that a ServiceLoader can find it -
 * i.e. it must be specified in the <code>
 * META-INF/services/org.eclipse.microprofile.rest.client.spi.RestClientBuilderListener
 * </code> file in an archive on the current thread's context classloader's
 * class path.
 *
 * Note that the <code>onNewBuilder</code> method will be called when the
 * RestClientBuilder is constructed, not when it's <code>build</code> method is
 * invoked.  This allows the caller to override global providers if they desire.
 *
 * @since 1.1
 */
public interface RestClientBuilderListener {

    void onNewBuilder(RestClientBuilder builder);
}
