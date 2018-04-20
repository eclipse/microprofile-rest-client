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

import java.net.URI;
import java.net.URL;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithURLRequestFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

public class ProvidesRestClientBuilderTest extends Arquillian{

    @Deployment
    public static Archive<?> createDeployment() {
        String simpleName = ProvidesRestClientBuilderTest.class.getSimpleName();
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
                .addClasses(SimpleGetApi.class, ReturnWithURLRequestFilter.class);
    }

    @Test
    public void testCanCallStaticLoader() {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        assertNotNull(builder);
    }

    @Test
    public void testLastBaseUriOrBaseUrlCallWins() throws Exception {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        builder = builder.register(ReturnWithURLRequestFilter.class);

        builder = builder.baseUri(new URI("http://localhost:8080/wrong1"));
        builder = builder.baseUrl(new URL("http://localhost:8080/right1"));
        SimpleGetApi client = builder.build(SimpleGetApi.class);
        assertEquals(client.executeGet().readEntity(String.class), "GET http://localhost:8080/right1");

        builder = builder.baseUrl(new URL("http://localhost:8080/wrong2"));
        builder = builder.baseUri(new URI("http://localhost:8080/wrong2b"));
        builder = builder.baseUri(new URI("http://localhost:8080/right2"));
        client = builder.build(SimpleGetApi.class);
        assertEquals(client.executeGet().readEntity(String.class), "GET http://localhost:8080/right2");
    }

    @Test
    public void testIllegalStateExceptionThrownWhenNoBaseUriOrUrlSpecified() {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        builder = builder.register(ReturnWithURLRequestFilter.class);
        try {
            builder.build(SimpleGetApi.class);
            fail("Did not throw expected IllegalStateException");
        }
        catch (Throwable t) {
            assertEquals(t.getClass(), IllegalStateException.class);
        }
    }
}
