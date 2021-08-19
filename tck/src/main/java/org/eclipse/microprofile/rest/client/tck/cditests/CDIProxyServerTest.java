/*
 * Copyright 2020, 2021 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.tck.cditests;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.eclipse.microprofile.rest.client.tck.ProxyServerTest.DESTINATION_SERVER_PORT;
import static org.eclipse.microprofile.rest.client.tck.ProxyServerTest.startDestinationServer;
import static org.eclipse.microprofile.rest.client.tck.ProxyServerTest.stopDestinationServer;
import static org.testng.Assert.assertEquals;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.ProxyServerTest;
import org.eclipse.microprofile.rest.client.tck.WiremockArquillianTest;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

/**
 * Verifies via CDI injection that you can use a programmatic interface. verifies that the interface has Dependent
 * scope.
 */
public class CDIProxyServerTest extends WiremockArquillianTest {
    @Inject
    @RestClient
    private SimpleGetApi client;

    @Deployment
    public static WebArchive createDeployment() {
        String uriProperty = SimpleGetApi.class.getName() + "/mp-rest/uri=" + getStringURL() + "testProxyCDI";
        String proxyProperty = SimpleGetApi.class.getName() + "/mp-rest/proxyAddress=localhost:" + getPort();
        String simpleName = CDIProxyServerTest.class.getSimpleName();
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, simpleName + ".jar")
                .addClasses(SimpleGetApi.class, WiremockArquillianTest.class, ProxyServerTest.class)
                .addAsManifestResource(new StringAsset(String.format(uriProperty + "%n" + proxyProperty)),
                        "microprofile-config.properties")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
                .addAsLibrary(jar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testProxy() throws Exception {
        stubFor(get(urlMatching("/.*")).willReturn(
                aResponse().proxiedFrom("http://localhost:" + DESTINATION_SERVER_PORT)
                        .withAdditionalRequestHeader("X-Via", "CDIWireMockProxy")));
        try {
            startDestinationServer("bar");
            Response response = client.executeGet();
            assertEquals(response.getStatus(), 200);
            assertEquals(response.readEntity(String.class).trim(), "bar");
            assertEquals(response.getHeaderString("X-Via"), "CDIWireMockProxy");
        } finally {
            stopDestinationServer();
        }
    }
}
