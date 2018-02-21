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

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.testng.Assert.assertEquals;

public class InvokeSimpleGetOperationTest extends WiremockArquillianTest{
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, InvokeSimpleGetOperationTest.class.getSimpleName()+".war")
            .addClasses(SimpleGetApi.class, WiremockArquillianTest.class);
    }

    @Test
    public void testGetExecutionWithBuiltClient() throws Exception{
        String expectedBody = "Hello, MicroProfile!";
        stubFor(get(urlEqualTo("/"))
            .willReturn(aResponse()
                .withBody(expectedBody)));

        SimpleGetApi simpleGetApi = RestClientBuilder.newBuilder()
            .baseUrl(getServerURL())
            .build(SimpleGetApi.class);

        Response response = simpleGetApi.executeGet();

        String body = response.readEntity(String.class);

        response.close();

        assertEquals(body, expectedBody);

        verify(1, getRequestedFor(urlEqualTo("/")));
    }
}
