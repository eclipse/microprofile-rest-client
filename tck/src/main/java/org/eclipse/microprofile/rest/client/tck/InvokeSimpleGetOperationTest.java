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

package org.eclipse.microprofile.rest.client.tck;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.testng.Assert.assertEquals;

public class InvokeSimpleGetOperationTest extends Arquillian{
    private static int port;
    private WireMockServer wireMockServer;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(SimpleGetApi.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

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

    @Test
    public void testInvokesOperation() throws Exception{
        String expectedBody = "Hello, MicroProfile!";
        wireMockServer.stubFor(get(urlEqualTo("/"))
            .willReturn(aResponse()
                .withBody(expectedBody)));

        SimpleGetApi simpleGetApi = RestClientBuilder.newBuilder()
            .baseUrl(new URL("http://localhost:"+ port))
            .build(SimpleGetApi.class);

        Response response = simpleGetApi.executeGet();

        String body = response.readEntity(String.class);

        response.close();

        assertEquals(body, expectedBody);

        wireMockServer.verify(1, getRequestedFor(urlEqualTo("/")));
    }
}
