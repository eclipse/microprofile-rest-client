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
package org.eclipse.microprofile.rest.client.tck.ssl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.entity.ContentType;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 *
 * HTTPS server which returns {@link #responseContent} on each request.
 *
 * Use {@link #keyStore(String, String)} and {@link #trustStore(String, String)} to set, appropriately, the server key
 * store and trust store
 *
 */
public class HttpsServer {
    private static final String CONTENT_TYPE = "Content-Type";

    private final Server server = new Server();
    private SslContextFactory sslContextFactory = new SslContextFactory();

    private String responseContent = "{\"foo\": \"bar\"}";
    private String responseContentType = ContentType.APPLICATION_JSON.getMimeType();

    public HttpsServer keyStore(String keystore, String keyStorePassword) {
        sslContextFactory.setKeyStorePath(keystore);
        sslContextFactory.setKeyStorePassword(keyStorePassword);
        sslContextFactory.setKeyStoreType("pkcs12");
        return this;
    }

    public HttpsServer trustStore(String keystore, String keyStorePassword) {
        sslContextFactory.setTrustStorePath(keystore);
        sslContextFactory.setTrustStorePassword(keyStorePassword);
        sslContextFactory.setTrustStoreType("pkcs12");
        sslContextFactory.setNeedClientAuth(true);
        sslContextFactory.setEndpointIdentificationAlgorithm(null);
        return this;
    }

    public HttpsServer start(int httpsPort, String httpsHostname) {
        server.setHandler(
                new AbstractHandler() {
                    @Override
                    public void handle(String path,
                            Request request,
                            HttpServletRequest httpRequest,
                            HttpServletResponse response) throws IOException {
                        response.setHeader(CONTENT_TYPE, responseContentType);
                        try (PrintWriter writer = response.getWriter()) {
                            writer.println(responseContent);
                        }
                    }
                });
        // SSL HTTP Configuration
        HttpConfiguration httpsConfig = new HttpConfiguration(); // httpConfig);
        httpsConfig.setSecureScheme("https");
        httpsConfig.setSecurePort(httpsPort);

        ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(httpsConfig));

        sslConnector.setPort(httpsPort);
        sslConnector.setHost(httpsHostname);
        server.addConnector(sslConnector);
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start https server", e);
        }
        return this;
    }

    public void stop() {
        try {
            server.stop();
        } catch (Exception e) {
            throw new RuntimeException("Failed to stop https server", e);
        }
    }
}
