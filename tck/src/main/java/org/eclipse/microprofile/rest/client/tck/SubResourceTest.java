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

import static org.testng.Assert.*;

import java.net.URI;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.RootResource;
import org.eclipse.microprofile.rest.client.tck.interfaces.SubResource;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithURLRequestFilter;
import org.eclipse.microprofile.rest.client.tck.providers.TestResponseExceptionMapper;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public class SubResourceTest extends Arquillian {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, SubResourceTest.class.getSimpleName() + ".war")
                .addClasses(RootResource.class, SubResource.class, TestResponseExceptionMapper.class)
                .addPackage(ReturnWithURLRequestFilter.class.getPackage());
    }

    @BeforeTest
    public void resetHandlers() {
        TestResponseExceptionMapper.reset();
    }

    @Test
    public void canInvokeMethodOnSubResourceInterface() throws Exception {
        ReturnWithURLRequestFilter filter = new ReturnWithURLRequestFilter();
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(filter);
        RootResource client = builder.baseUri(new URI("http://localhost/stub")).build(RootResource.class);
        SubResource subClient = client.sub();
        assertNotNull(subClient, "SubResource interface is null");
        Response response = subClient.getFromSub();
        assertEquals(response.getStatus(), 200, "Unexpected response status code");
        String responseStr = response.readEntity(String.class);
        assertNotNull(responseStr, "Response entity is null");
        assertTrue(responseStr.contains("GET ") && responseStr.contains("/root/sub"),
                "Did not invoke expected method/URI. Expected GET .../root/sub ; got " + responseStr);
    }

    @Test
    public void exceptionMappedOnSubResourceInterface() throws Exception {
        ReturnWithURLRequestFilter filter = new ReturnWithURLRequestFilter();

        RestClientBuilder builder = RestClientBuilder.newBuilder()
                .register(filter)
                .register(TestResponseExceptionMapper.class);

        RootResource client = builder.baseUri(new URI("http://localhost/stub")).build(RootResource.class);
        SubResource subClient = client.sub();
        assertNotNull(subClient, "SubResource interface is null");

        try {
            Response response = subClient.getFromSub();
            fail("A " + WebApplicationException.class + " should have been thrown via the registered "
                    + TestResponseExceptionMapper.class);
        } catch (WebApplicationException w) {
            assertEquals(w.getMessage(), TestResponseExceptionMapper.MESSAGE,
                    "The message should be sourced from " + TestResponseExceptionMapper.class);
            assertTrue(TestResponseExceptionMapper.isHandlesCalled(),
                    "The handles method should have been called on " + TestResponseExceptionMapper.class);
            assertTrue(TestResponseExceptionMapper.isThrowableCalled(),
                    "The toThrowable method should have been called on " + TestResponseExceptionMapper.class);
        }
    }
}
