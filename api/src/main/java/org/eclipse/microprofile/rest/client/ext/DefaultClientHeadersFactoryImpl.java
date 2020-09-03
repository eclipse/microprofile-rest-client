/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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

import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * This class propagates JAX-RS headers whose names are specified using the
 * MicroProfile Config property,
 * <code>org.eclipse.microprofile.rest.client.propagateHeaders</code>.
 *
 * <p>The value of this property should be a comma-separated list of HTTP header
 * names. If the headers specified in the property exist in the inbound JAX-RS
 * request, this class will propagate those headers to the outbound Rest Client
 * request.
 *
 * <p>Any headers present on {@code clientOutgoingHeaders} will not be returned when calling the
 * {@link #update(MultivaluedMap, MultivaluedMap) update} method.
 *
 * @since 1.2
 */
public class DefaultClientHeadersFactoryImpl implements ClientHeadersFactory {

    public final static String PROPAGATE_PROPERTY = "org.eclipse.microprofile.rest.client.propagateHeaders";
    private final static String CLASS_NAME = DefaultClientHeadersFactoryImpl.class.getName();
    private final static Logger LOG = Logger.getLogger(CLASS_NAME);

    private static Optional<Config> config() {
        try {
            return Optional.ofNullable(ConfigProvider.getConfig());
        }
        catch (ExceptionInInitializerError | NoClassDefFoundError | IllegalStateException ex) {
            // expected if no MP Config implementation is available
            return Optional.empty();
        }
    }

    private static Optional<String> getHeadersProperty() {
        Optional<Config> c = config();
        if (c.isPresent()) {
            return Optional.ofNullable(c.get().getOptionalValue(PROPAGATE_PROPERTY, String.class).orElse(null));
        }
        return Optional.empty();
    }

    @Override
    public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
                                                 MultivaluedMap<String, String> clientOutgoingHeaders) {

        if (LOG.isLoggable(Level.FINER)) {
            LOG.entering(CLASS_NAME, "update", new Object[]{incomingHeaders, clientOutgoingHeaders});
        }
        MultivaluedMap<String, String> propagatedHeaders = new MultivaluedHashMap<>();
        Optional<String> propagateHeaderString = getHeadersProperty();
        if (propagateHeaderString.isPresent()) {
            Arrays.stream(propagateHeaderString.get().split(","))
                  .forEach( header -> {
                      if (incomingHeaders.containsKey(header)) {
                          propagatedHeaders.put(header, incomingHeaders.get(header));
                      }
                  });
        }
        if (LOG.isLoggable(Level.FINER)) {
            LOG.exiting(CLASS_NAME, "update", propagatedHeaders);
        }
        return propagatedHeaders;
    }
}
