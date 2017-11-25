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

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.eclipse.microprofile.rest.client.tck.providers.TestResponseExceptionMapper;
import org.eclipse.microprofile.rest.client.tck.providers.TestResponseExceptionMapperOverridePriority;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class DefaultExceptionMapperTest extends WiremockArquillianTest {

    private static final int STATUS = 401;
    public static final String BODY = "body is used by this test";

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, DefaultExceptionMapperTest.class.getSimpleName()+".war")
            .addClasses(SimpleGetApi.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @BeforeTest
    public void resetHandlers() {
        TestResponseExceptionMapper.reset();
        TestResponseExceptionMapperOverridePriority.reset();
        wireMockServer
            .stubFor(get(urlEqualTo("/")).willReturn(aResponse().withStatus(STATUS).withBody(BODY)));
    }

    @Test
    public void testWithOneRegisteredProvider() throws Exception {
        SimpleGetApi simpleGetApi = RestClientBuilder.newBuilder()
            .baseUrl(new URL("http://localhost:"+ port))
            .register(TestResponseExceptionMapper.class)
            .build(SimpleGetApi.class);

        try {
            simpleGetApi.executeGet();
            fail("A "+WebApplicationException.class+" should have been thrown automatically");
        }
        catch (WebApplicationException w) {
            Response response = w.getResponse();
            // the response should have the response code from the api call
            assertEquals(response.getStatus(), STATUS,
                "The 401 from the response should be propagated");
            String body = response.readEntity(String.class);
            assertEquals(body, BODY,
                "The body of the response should be propagated");
            response.close();
        }
    }
}
