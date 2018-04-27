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

package org.eclipse.microprofile.rest.client.tck.cditests;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.WiremockArquillianTest;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.ws.rs.core.Response;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.testng.Assert.assertEquals;

/**
 * Verifies that URI declared via MP Config takes precedence over URL.
 */
public class CDIURIvsURLConfigTest extends WiremockArquillianTest{

    @Inject
    @RestClient
    private SimpleGetApi api;

    @Deployment
    public static WebArchive createDeployment() {
        String uriPropertyName = SimpleGetApi.class.getName()+"/mp-rest/uri";
        String uriValue = getStringURL() + "uri";
        String urlPropertyName = SimpleGetApi.class.getName()+"/mp-rest/url";
        String urlValue = getStringURL() + "url";
        String simpleName = CDIURIvsURLConfigTest.class.getSimpleName();
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, simpleName + ".jar")
            .addClasses(SimpleGetApi.class, WiremockArquillianTest.class)
            .addAsManifestResource(new StringAsset(
                String.format(uriPropertyName+"="+uriValue+"%n"+urlPropertyName+"="+urlValue)),
                "microprofile-config.properties");
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
            .addAsLibrary(jar);
    }

    @Test
    public void testURItakesPrecedenceOverURL() throws Exception{
        String expectedBody = "Hello, MicroProfile URI!!";
        stubFor(get(urlEqualTo("/uri"))
            .willReturn(aResponse()
                .withBody(expectedBody)));

        stubFor(get(urlEqualTo("/url"))
            .willReturn(aResponse()
                .withBody("Using URL instead of URI")));

        Response response = api.executeGet();

        String body = response.readEntity(String.class);

        response.close();

        assertEquals(body, expectedBody);

        verify(1, getRequestedFor(urlEqualTo("/uri")));
    }
}
