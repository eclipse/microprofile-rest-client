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
import org.eclipse.microprofile.rest.client.tck.providers.TestResponseExceptionMapperHandles;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import javax.ws.rs.WebApplicationException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class CallMultipleMappersTest extends WiremockArquillianTest {

    @Deployment
    public static Archive<?> createDeployment() {
        String simpleName = CallMultipleMappersTest.class.getSimpleName();
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
                .addClasses(WiremockArquillianTest.class,
                            SimpleGetApi.class,
                            TestResponseExceptionMapper.class,
                            TestResponseExceptionMapperHandles.class);
    }

    @Test
    public void testCallsTwoProvidersWithTwoRegisteredProvider() throws Exception {
        TestResponseExceptionMapper.reset();
        TestResponseExceptionMapperHandles.reset();
        stubFor(get(urlEqualTo("/")).willReturn(aResponse().withBody("body is ignored in this test")));
        SimpleGetApi simpleGetApi = RestClientBuilder.newBuilder()
            .baseUri(getServerURI())
            .register(TestResponseExceptionMapper.class)
            .register(TestResponseExceptionMapperHandles.class)
            .build(SimpleGetApi.class);

        try {
            simpleGetApi.executeGet();
            fail("A "+WebApplicationException.class+" should have been thrown via the registered "+TestResponseExceptionMapper.class);
        }
        catch (WebApplicationException w) {
            assertEquals(w.getMessage(), TestResponseExceptionMapper.MESSAGE,
                "The message should be sourced from "+TestResponseExceptionMapper.class);
            assertTrue(TestResponseExceptionMapper.isHandlesCalled(),
                "The handles method should have been called on "+TestResponseExceptionMapper.class);
            assertTrue(TestResponseExceptionMapper.isThrowableCalled(),
                "The toThrowable method should have been called on "+TestResponseExceptionMapper.class);
            // it should still have called the other mapper
            assertTrue(TestResponseExceptionMapperHandles.isHandlesCalled(),
                "The handles method should have been called on "+TestResponseExceptionMapperHandles.class);
            assertTrue(TestResponseExceptionMapperHandles.isThrowableCalled(),
                "The toThrowable method should have been called on "+TestResponseExceptionMapperHandles.class);
        }
    }
}
