/*
 * Copyright (c) 2017-2020 Contributors to the Eclipse Foundation
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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.KeyStore;
import java.security.PrivilegedAction;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.core.Configurable;
import java.util.concurrent.ExecutorService;

import org.eclipse.microprofile.rest.client.ext.QueryParamStyle;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderListener;
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
        RestClientBuilder builder = RestClientBuilderResolver.instance().newBuilder();
        AccessController.doPrivileged((PrivilegedAction<Void>)
            () -> {
                for(RestClientBuilderListener listener : ServiceLoader.load(RestClientBuilderListener.class)) {
                    listener.onNewBuilder(builder);
                }
                return null;
            }
        );
        return builder;
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
     * Subsequent calls to this method will replace the previously specified
     * baseUri/baseUrl.
     *
     * @param url the base Url for the service.
     * @return the current builder with the baseUrl set.
     */
    RestClientBuilder baseUrl(URL url);

    /**
     * Specifies the base URI to be used when making requests. Assuming that the
     * interface has a <code>@Path("/api")</code> at the interface level and a
     * <code>uri</code> is given with
     * <code>http://my-service:8080/service</code> then all REST calls will be
     * invoked with a <code>uri</code> of
     * <code>http://my-service:8080/service/api</code> in addition to any
     * <code>@Path</code> annotations included on the method.
     *
     * Subsequent calls to this method will replace the previously specified
     * baseUri/baseUrl.
     *
     * @param uri the base URI for the service.
     * @return the current builder with the baseUri set
     * @throws IllegalArgumentException if the passed in URI is invalid
     * @since 1.1
     */
    default RestClientBuilder baseUri(URI uri) {
        try {
            return baseUrl(uri.toURL());
        }
        catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    /**
     * Set the connect timeout.
     * <p>
     * Like JAX-RS's <code>javax.ws.rs.client.ClientBuilder</code>'s
     * <code>connectTimeout</code> method, specifying a timeout of 0 represents
     * infinity, and negative values are not allowed.
     * </p>
     * <p>
     * If the client instance is injected via CDI and the
     * &quot;<em>fully.qualified.InterfaceName</em>/mp-rest/connectTimeout&quot;
     * property is set via MicroProfile Config, that property's value will
     * override, the value specified to this method.
     * </p>
     *
     * @param timeout the maximum time to wait.
     * @param unit the time unit of the timeout argument.
     * @return the current builder with the connect timeout set.
     * @throws IllegalArgumentException - if the value of timeout is negative.
     * @since 1.2
     */
    RestClientBuilder connectTimeout(long timeout, TimeUnit unit);

    /**
     * Set the read timeout.
     * <p>
     * Like JAX-RS's <code>javax.ws.rs.client.ClientBuilder</code>'s
     * <code>readTimeout</code> method, specifying a timeout of 0 represents
     * infinity, and negative values are not allowed.
     * </p>
     * <p>
     * Also like the JAX-RS Client API, if the read timeout is reached, the
     * client interface method will throw a <code>javax.ws.rs.ProcessingException</code>.
     * </p>
     * <p>
     * If the client instance is injected via CDI and the
     * &quot;<em>fully.qualified.InterfaceName</em>/mp-rest/readTimeout&quot;
     * property is set via MicroProfile Config, that property's value will
     * override, the value specified to this method.
     * </p>
     *
     * @param timeout the maximum time to wait.
     * @param unit the time unit of the timeout argument.
     * @return the current builder with the connect timeout set.
     * @throws IllegalArgumentException - if the value of timeout is negative.
     * @since 1.2
     */
    RestClientBuilder readTimeout(long timeout, TimeUnit unit);

    /**
     * Specifies the <code>ExecutorService</code> to use when invoking
     * asynchronous Rest Client interface methods.  By default, the executor
     * service used is determined by the MP Rest Client implementation runtime.
     *
     * @param executor the executor service for the runtime to use when invoking
     * asynchronous Rest Client interface methods - must be non-null.
     * @return the current builder with the executorService set.
     * @throws IllegalArgumentException if the <code>executor</code> parameter is
     * null.
     * @since 1.1
     */
    RestClientBuilder executorService(ExecutorService executor);

    /**
     * Specifies the SSL context to use when creating secured transport connections to server endpoints
     * from web targets created by the client instance that is using this SSL context.
     *
     * @param sslContext the ssl context
     * @return the current builder with ssl context set
     * @throws NullPointerException if the <code>sslContext</code> parameter is
     * null.
     * @since 1.3
     */
    RestClientBuilder sslContext(SSLContext sslContext);


    /**
     * Set the client-side trust store.
     *
     * @param trustStore key store
     * @return the current builder with the trust store set
     * @throws NullPointerException if the <code>trustStore</code> parameter is
     * null.
     * @since 1.3
     */
    RestClientBuilder trustStore(KeyStore trustStore);

    /**
     * Set the client-side key store.
     *
     * @param keyStore key store
     * @param keystorePassword the password for the specified <code>keyStore</code>
     * @return the current builder with the key store set
     * @throws NullPointerException if the <code>keyStore</code> parameter is
     * null.
     * @since 1.3
     */
    RestClientBuilder keyStore(KeyStore keyStore, String keystorePassword);

    /**
     * Set the hostname verifier to verify the endpoint's hostname
     *
     * @param hostnameVerifier the hostname verifier
     * @return the current builder with hostname verifier set
     * @throws NullPointerException if the <code>hostnameVerifier</code> parameter is
     * null.
     * @since 1.3
     */
    RestClientBuilder hostnameVerifier(HostnameVerifier hostnameVerifier);

    /**
     * Specifies whether client built by this builder should follow HTTP
     * redirect responses (30x) or not.
     * 
     * @param follow - true if the client should follow HTTP redirects, false if not.
     * @return the current builder with the followRedirect property set.
     * @since 2.0
     */
    RestClientBuilder followRedirects(boolean follow);

    /**
     * Specifies the HTTP proxy hostname/IP address and port to use for requests from client instances.
     * 
     * @param proxyHost - hostname or IP address of proxy server - must be non-null
     * @param proxyPort - port of proxy server
     * @throws IllegalArgumentException if the <code>proxyHost</code> is null or the <code>proxyPort</code> is invalid
     * @return the current builder with the proxy host set
     * @since 2.0
     */
    RestClientBuilder proxyAddress(String proxyHost, int proxyPort);

    /**
     * Specifies the URI formatting style to use when multiple query parameter values are passed to the client.
     * 
     * @param style - the URI formatting style to use for multiple query parameter values
     * @return the current builder with the style of query params set
     * @since 2.0
     */
    RestClientBuilder queryParamStyle(QueryParamStyle style);

    /**
     * Based on the configured RestClientBuilder, creates a new instance of the
     * given REST interface to invoke API calls against.
     *
     * @param clazz the interface that defines REST API methods for use
     * @param <T> the type of the interface
     * @return a new instance of an implementation of this REST interface that
     * @throws IllegalStateException if not all pre-requisites are satisfied for
     * the builder, this exception may get thrown. For instance, if the base
     * URI/URL has not been set.
     * @throws RestClientDefinitionException if the passed-in interface class is
     * invalid.
     */
    <T> T build(Class<T> clazz) throws IllegalStateException, RestClientDefinitionException;

}
