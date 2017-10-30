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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public abstract class WiremockArquillianTest extends Arquillian{
    protected static int port;
    protected WireMockServer wireMockServer;

    @BeforeClass
    public static void getPort() {
        port = Integer.parseInt(System.getProperty("wiremock.server.port","8765"));
    }

    @BeforeMethod
    public void setupMockServer() {
        wireMockServer = new WireMockServer(options().port(port));
        wireMockServer.start();
    }

    @AfterMethod
    public void stopServer() {
        wireMockServer.stop();
    }
}
