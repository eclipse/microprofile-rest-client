/*
 * Copyright 2017, 2021 Contributors to the Eclipse Foundation
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.Response;

public class DefaultExceptionMapperConfigTest extends WiremockArquillianTest {
    private static final int STATUS = 401;
    private static final String BODY = "body is used by this test";

    @Deployment
    public static Archive<?> createDeployment() {
        StringAsset mpConfig = new StringAsset("microprofile.rest.client.disable.default.mapper=true");
        return ShrinkWrap.create(WebArchive.class, DefaultExceptionMapperConfigTest.class.getSimpleName() + ".war")
                .addAsWebInfResource(mpConfig, "classes/META-INF/microprofile-config.properties")
                .addClasses(SimpleGetApi.class, WiremockArquillianTest.class);
    }

    @Test
    public void testNoExceptionThrownWhenDisabledDuringBuild() throws Exception {
        stubFor(get(urlEqualTo("/")).willReturn(aResponse().withStatus(STATUS).withBody(BODY)));

        SimpleGetApi simpleGetApi = RestClientBuilder.newBuilder()
                .baseUri(getServerURI())
                .build(SimpleGetApi.class);

        try {
            Response response = simpleGetApi.executeGet();
            assertEquals(response.getStatus(), STATUS);
        } catch (Exception w) {
            fail("No exception should be thrown", w);
        }
    }
}
