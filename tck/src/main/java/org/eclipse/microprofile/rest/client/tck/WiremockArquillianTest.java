/*
 * Copyright 2017 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.microprofile.rest.client.tck;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.arquillian.testng.Arquillian;
import org.testng.annotations.BeforeClass;

import com.github.tomakehurst.wiremock.client.WireMock;

public abstract class WiremockArquillianTest extends Arquillian {
    private static Integer port;
    private static String host;
    private static String scheme;
    private static String context;

    public static Integer getPort() {
        if (port == null) {
            setupWireMockConnection();
        }
        return port;
    }

    protected static URI getServerURI() {
        try {
            return new URI(getStringURL());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Malformed URI not expected", e);
        }
    }

    protected static URL getServerURL() {
        try {
            return new URL(getStringURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL not expected", e);
        }
    }

    protected static String getStringURL() {
        int port = getPort();
        return scheme + "://" + host + ":" + port + "" + context;
    }

    @BeforeClass
    public static void setupServer() {
        setupWireMockConnection();
        WireMock.configureFor(scheme, host, port, context);
        WireMock.reset();
    }

    private static void setupWireMockConnection() {
        host = System.getProperty("wiremock.server.host", "localhost");
        port = Integer.parseInt(System.getProperty("wiremock.server.port", "8765"));
        scheme = System.getProperty("wiremock.server.scheme", "http");
        context = System.getProperty("wiremock.server.context", "/");
    }
}
