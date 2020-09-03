/*
 * Copyright (c) 2019, 2020 Contributors to the Eclipse Foundation
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

import javax.ws.rs.core.MultivaluedMap;

/**
 * This interface is intended for generating or propagating HTTP headers. It is
 * invoked by the MP Rest Client implementation before invoking any entity
 * providers on the outbound processing chain. It contains a single method,
 * <code>update</code> which takes parameters of headers passed in from the
 * incoming JAX-RS request (if applicable, if not, this will be an empty map)
 * and a read-only map of headers specified by <code>ClientHeaderParam</code> or
 * <code>HeaderParam</code> annotations on the client interface.
 * <p>
 * This method should return a MultivaluedMap of headers to be merged with the
 * outgoing headers. If it's desired for {@code clientOutgoingHeaders} to be present in
 * addition to any propagated headers, {@link #update(MultivaluedMap, MultivaluedMap) update}
 * needs to combine the two sets to return. This will determine the final set of HTTP headers that will
 * be sent to the outbound entity provider processing chain - thus any filters,
 * MessageBodyWriters, interceptors, etc. could further refine the set of
 * headers actually sent on the client request.
 * <p>
 * If the ClientHeadersFactory instance is invoked while in the context of a
 * JAX-RS request, the implementation may optionally support injection of fields
 * and methods annotated with <code>{@literal @}Context</code>.
 * <p>
 * If the ClientHeadersFactory instance is managed by CDI (i.e. it is annotated
 * with <code>{@literal @}ApplicationScoped</code>, etc.), the implementation
 * must use the appropriate CDI-managed instance, and must support
 * <code>{@literal @}Inject</code> injection.
 *
 * @since 1.2
 */
public interface ClientHeadersFactory {

    /**
     * Updates the HTTP headers to send to the remote service. Note that providers
     * on the outbound processing chain could further update the headers.
     *
     * @param incomingHeaders - the map of headers from the inbound JAX-RS request. This will
     * be an empty map if the associated client interface is not part of a JAX-RS request.
     * @param clientOutgoingHeaders - the read-only map of header parameters specified on the
     * client interface.
     * @return a map of HTTP headers to merge with the clientOutgoingHeaders to be sent to
     * the remote service.
     */
    MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
                                          MultivaluedMap<String, String> clientOutgoingHeaders);
}
