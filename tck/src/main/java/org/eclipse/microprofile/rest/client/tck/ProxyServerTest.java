/*
 * Copyright (c) 2020, 2021 Contributors to the Eclipse Foundation
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.Response;

public class ProxyServerTest extends WiremockArquillianTest {
    public static final int DESTINATION_SERVER_PORT =
            Integer.getInteger("org.eclipse.microprofile.rest.client.ssl.port", 8948);
    private static final Server DESTINATION_SERVER = new Server(DESTINATION_SERVER_PORT);

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, ProxyServerTest.class.getSimpleName() + ".war")
                .addClasses(SimpleGetApi.class, WiremockArquillianTest.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNullHostName() throws Exception {
        RestClientBuilder.newBuilder().proxyAddress(null, 1234);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidPortNumber() throws Exception {
        RestClientBuilder.newBuilder().proxyAddress("microprofile.io", 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidPortNumber1() throws Exception {
        RestClientBuilder.newBuilder().proxyAddress("microprofile.io", -1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidPortNumber2() throws Exception {
        RestClientBuilder.newBuilder().proxyAddress("microprofile.io", 65536);
    }

    @Test
    public void testProxy() throws Exception {
        stubFor(get(urlMatching("/.*")).willReturn(
                aResponse().proxiedFrom("http://localhost:" + DESTINATION_SERVER_PORT)
                        .withAdditionalRequestHeader("X-Via", "WireMockProxy")));
        try {
            startDestinationServer("foo");
            SimpleGetApi client = RestClientBuilder.newBuilder()
                    .proxyAddress("localhost", getPort())
                    .baseUri(URI.create("http://localhost:" + DESTINATION_SERVER_PORT + "/testProxy"))
                    .build(SimpleGetApi.class);
            Response response = client.executeGet();
            assertEquals(response.getStatus(), 200);
            assertEquals(response.readEntity(String.class).trim(), "foo");
            assertEquals(response.getHeaderString("X-Via"), "WireMockProxy");
        } finally {
            stopDestinationServer();
        }
    }

    public static void startDestinationServer(String responseContent) {
        DESTINATION_SERVER.setHandler(
                new AbstractHandler() {
                    @Override
                    public void handle(String path,
                            Request request,
                            HttpServletRequest httpRequest,
                            HttpServletResponse response) throws IOException {
                        response.setHeader("Content-Type", "text/plain");
                        response.setHeader("X-Via", request.getHeader("X-Via"));
                        try (PrintWriter writer = response.getWriter()) {
                            writer.println(responseContent);
                        }
                    }
                });
        try {
            DESTINATION_SERVER.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start destination server", e);
        }
    }

    public static void stopDestinationServer() {
        try {
            DESTINATION_SERVER.stop();
        } catch (Exception e) {
            throw new RuntimeException("Failed to stop destination server", e);
        }
    }
}
