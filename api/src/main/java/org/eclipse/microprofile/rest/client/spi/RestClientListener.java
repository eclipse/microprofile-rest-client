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
 * Implementations of this interface will be notified when new Rest Client
 * instances are being constructed.  This will allow implementations to register
 * providers on the RestClientBuilder, and is intended for global providers.
 *
 * In order for the RestClientBuilder implementation to call implementations of
 * this interface, the implementation must be specified such that
 * <code>ServiceLoader</code> can find it - i.e. it must be specified in the <code>
 * META-INF/services/org.eclipse.microprofile.rest.client.spi.RestClientListener
 * </code> file in an archive on the current thread's context classloader's
 * class path - or specified in the <code>module-info.java</code>.
 *
 * Note that the <code>onNewClient</code> method will be called when the
 * RestClientBuilder's <code>build(Class&lt;?&gt; serviceInterface)</code> method is
 * called.
 *
 * @since 1.2
 */
public interface RestClientListener {

    void onNewClient(Class<?> serviceInterface, RestClientBuilder builder);
}
