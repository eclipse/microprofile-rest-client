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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.BaseClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.ChildClient;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithURLRequestFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.Response;

public class InheritanceTest extends Arquillian {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, InheritanceTest.class.getSimpleName() + ".war")
                .addPackage(ReturnWithURLRequestFilter.class.getPackage())
                .addClasses(BaseClient.class, ChildClient.class);
    }

    @Test
    public void canInvokeMethodOnBaseInterface() throws Exception {
        ReturnWithURLRequestFilter filter = new ReturnWithURLRequestFilter();
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(filter);
        BaseClient client = builder.baseUri(new URI("http://localhost/stub")).build(ChildClient.class);
        Response response = client.executeBaseGet();
        assertEquals(response.getStatus(), 200, "Unexpected response status code");
        String responseStr = response.readEntity(String.class);
        assertNotNull(responseStr, "Response entity is null");
        assertTrue(responseStr.contains("GET ") && responseStr.contains("/base"),
                "Did not invoke expected method/URI. Expected GET .../base ; got " + responseStr);
    }

    @Test
    public void canInvokeMethodOnChildInterface() throws Exception {
        ReturnWithURLRequestFilter filter = new ReturnWithURLRequestFilter();
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(filter);
        ChildClient client = builder.baseUri(new URI("http://localhost/stub")).build(ChildClient.class);
        Response response = client.executeChildGet();
        assertEquals(response.getStatus(), 200, "Unexpected response status code");
        String responseStr = response.readEntity(String.class);
        assertNotNull(responseStr, "Response entity is null");
        assertTrue(responseStr.contains("GET ") && responseStr.contains("/child"),
                "Did not invoke expected method/URI. Expected GET .../child ; got " + responseStr);
    }

    @Test
    public void canInvokeOverriddenMethodOnChildInterface() throws Exception {
        ReturnWithURLRequestFilter filter = new ReturnWithURLRequestFilter();
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(filter);
        BaseClient client = builder.baseUri(new URI("http://localhost/stub")).build(ChildClient.class);
        Response response = client.executeBasePost();
        assertEquals(response.getStatus(), 200, "Unexpected response status code");
        String responseStr = response.readEntity(String.class);
        assertNotNull(responseStr, "Response entity is null");
        assertTrue(responseStr.contains("POST ") && responseStr.contains("/childOverride"),
                "Did not invoke expected method/URI. Expected POST .../childOverride ; got " + responseStr);
    }
}
