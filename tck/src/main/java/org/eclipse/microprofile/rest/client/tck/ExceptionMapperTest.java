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
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.ws.rs.WebApplicationException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class ExceptionMapperTest extends WiremockArquillianTest{

    @Deployment
    public static Archive<?> createDeployment() {
        String simpleName = ExceptionMapperTest.class.getSimpleName();
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
            .addClasses(WiremockArquillianTest.class,
                        SimpleGetApi.class,
                        TestResponseExceptionMapper.class,
                        TestResponseExceptionMapperOverridePriority.class);
    }

    @BeforeTest
    public void resetHandlers() {
        TestResponseExceptionMapper.reset();
        TestResponseExceptionMapperOverridePriority.reset();
    }

    @Test
    public void testWithOneRegisteredProvider() throws Exception {
        stubFor(get(urlEqualTo("/")).willReturn(aResponse().withHeader("CustomHeader", "true")
            .withBody("body is ignored in this test")));
        SimpleGetApi simpleGetApi = RestClientBuilder.newBuilder()
            .baseUri(getServerURI())
            .register(TestResponseExceptionMapper.class)
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
        }
    }

    @Test
    public void testWithTwoRegisteredProviders() throws Exception{
        stubFor(get(urlEqualTo("/")).willReturn(aResponse().withHeader("CustomHeader", "true")
            .withBody("body is ignored in this test")));
        SimpleGetApi simpleGetApi = RestClientBuilder.newBuilder()
            .baseUri(getServerURI())
            .register(TestResponseExceptionMapper.class)
            .register(TestResponseExceptionMapperOverridePriority.class)
            .build(SimpleGetApi.class);

        try {
            simpleGetApi.executeGet();
            fail("A "+WebApplicationException.class+" should have been thrown via the registered "+TestResponseExceptionMapper.class);
        }
        catch (WebApplicationException w) {
            // only handles should be called on the new mapper, since it returns false
            assertTrue(TestResponseExceptionMapperOverridePriority.isHandlesCalled(),
                "The handles method should have been called on "+TestResponseExceptionMapperOverridePriority.class);
            assertFalse(TestResponseExceptionMapperOverridePriority.isThrowableCalled(),
                "The toThrowable method should not have been called on "+TestResponseExceptionMapperOverridePriority.class);
            // both should be called on the regular mapper
            assertEquals(w.getMessage(), TestResponseExceptionMapper.MESSAGE,
                "The message should be sourced from "+TestResponseExceptionMapper.class);
            assertTrue(TestResponseExceptionMapper.isHandlesCalled(),
                "The handles method should have been called on "+TestResponseExceptionMapper.class);
            assertTrue(TestResponseExceptionMapper.isThrowableCalled(),
                "The toThrowable method should have been called on "+TestResponseExceptionMapper.class);
        }
    }
}
