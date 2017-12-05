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

import com.github.tomakehurst.wiremock.WireMockServer;
import org.jboss.arquillian.testng.Arquillian;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.net.MalformedURLException;
import java.net.URL;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public abstract class WiremockArquillianTest extends Arquillian {
    private static Integer port;
    private static WireMockServer wireMockServer;

    private static Integer getPort() {
        if(port == null) {
            setupPort();
        }
        return port;
    }

    protected WireMockServer getWireMockServer() {
        return wireMockServer;
    }

    protected static URL getServerURL() {
        try {
            return new URL(getStringURL());
        }
        catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL not expected", e);
        }
    }

    protected static String getStringURL() {
        return "http://localhost:" + getPort();
    }

    @BeforeClass
    public static void setupServer() {
        setupPort();
        wireMockServer = new WireMockServer(options().port(getPort()));
        wireMockServer.start();
    }

    private static void setupPort() {
        port = Integer.parseInt(System.getProperty("wiremock.server.port", "8765"));
    }

    @AfterClass
    public static void stopServer() {
        wireMockServer.stop();
    }
}
