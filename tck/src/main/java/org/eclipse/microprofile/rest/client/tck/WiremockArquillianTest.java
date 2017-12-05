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

import com.github.tomakehurst.wiremock.client.WireMock;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.testng.Arquillian;
import org.testng.annotations.BeforeClass;

import java.net.MalformedURLException;
import java.net.URL;

@RunAsClient
public abstract class WiremockArquillianTest extends Arquillian {
    private static Integer port;
    private static String host;

    private static Integer getPort() {
        if(port == null) {
            setupWireMockConnection();
        }
        return port;
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
        int port = getPort();
        return "http://"+host+":" + port;
    }

    @BeforeClass
    public static void setupServer() {
        setupWireMockConnection();
        WireMock.configureFor(host, port);
        WireMock.reset();
    }

    private static void setupWireMockConnection() {
        host = System.getProperty("wiremock.server.host", "localhost");
        port = Integer.parseInt(System.getProperty("wiremock.server.port", "8765"));
    }
}
