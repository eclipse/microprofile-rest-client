/*
 * Copyright (c) 2017-2021 Contributors to the Eclipse Foundation
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

import java.net.URI;
import java.net.URL;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.eclipse.microprofile.rest.client.ext.QueryParamStyle;

import jakarta.annotation.Priority;

@Priority(1)
public class BuilderImpl1 extends AbstractBuilder {
    @Override
    public RestClientBuilder baseUrl(URL url) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public RestClientBuilder baseUri(URI uri) {
        throw new IllegalStateException("not implemented");
    }

    public RestClientBuilder executorService(ExecutorService executor) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public RestClientBuilder sslContext(SSLContext sslContext) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public RestClientBuilder trustStore(KeyStore trustStore) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public RestClientBuilder keyStore(KeyStore keyStore, String keystorePassword) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public RestClientBuilder hostnameVerifier(HostnameVerifier verifier) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public RestClientBuilder connectTimeout(long timeout, TimeUnit unit) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public RestClientBuilder readTimeout(long timeout, TimeUnit unit) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public RestClientBuilder followRedirects(boolean follow) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public RestClientBuilder proxyAddress(String proxyHost, int proxyPort) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public RestClientBuilder queryParamStyle(QueryParamStyle style) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public <T> T build(Class<T> clazz) {
        throw new IllegalStateException("not implemented");
    }
}
