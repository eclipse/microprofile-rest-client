/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.rest.client;

import javax.ws.rs.core.Configurable;
import java.net.URL;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderResolver;

/**
 * This is the main entry point for creating a Type Safe Rest Client.
 * <p>
 * Invoking {@link #newBuilder()} is intended to always create a new instance,
 * not use a cached version.
 * </p>
 * <p>
 * The <code>RestClientBuilder</code> is a {@link Configurable} class as defined
 * by JAX-RS. This allows a user to register providers, implementation specific
 * configuration.
 * </p>
 * <p>
 * Implementations are expected to implement this class and provide the instance
 * via the mechanism in {@link RestClientBuilderResolver#instance()}.
 * </p>
 */
public interface RestClientBuilder extends Configurable<RestClientBuilder> {

    static RestClientBuilder newBuilder() {
        return RestClientBuilderResolver.instance().newBuilder();
    }

    /**
     * Specifies the base URL to be used when making requests. Assuming that the
     * interface has a <code>@Path("/api")</code> at the interface level and a
     * <code>url</code> is given with
     * <code>http://my-service:8080/service</code> then all REST calls will be
     * invoked with a <code>url</code> of
     * <code>http://my-service:8080/service/api</code> in addition to any
     * <code>@Path</code> annotations included on the method.
     *
     * @param url the base Url for the service.
     * @return the current builder with the baseUrl set
     */
    RestClientBuilder baseUrl(URL url);

    /**
     * Based on the configured RestClientBuilder, creates a new instance of the
     * given REST interface to invoke API calls against.
     *
     * @param clazz the interface that defines REST API methods for use
     * @param <T> the type of the interface
     * @return a new instance of an implementation of this REST interface that
     * @throws IllegalStateException if not all pre-requisites are satisfied for
     * the builder, this exception may get thrown. For instance, if a URL has
     * not been set.
     * @throws RestClientDefinitionException if the passed-in interface class is
     * invalid.
     */
    <T> T build(Class<T> clazz) throws IllegalStateException, RestClientDefinitionException;

}
