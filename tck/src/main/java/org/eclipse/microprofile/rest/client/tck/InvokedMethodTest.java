/*
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
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

import static org.testng.Assert.assertEquals;

import java.net.URI;

import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.BaseClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.ChildClient;
import org.eclipse.microprofile.rest.client.tck.providers.InvokedMethodRequestFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

/**
 * This tests the <code>org.eclipse.microprofile.rest.client.invokedMethod</code> property
 * that implementations must provide in the `ClientRequestContext` of `ClientRequestFilter`s
 * and `ClientResponseFilter`s.
 */
public class InvokedMethodTest extends Arquillian {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, InvokedMethodTest.class.getSimpleName()+".war")
            .addClasses(InvokedMethodTest.class,
                        BaseClient.class,
                        ChildClient.class,
                        InvokedMethodRequestFilter.class);
    }

    /**
     * This test checks that the Rest Client implementation provides the
     * <i>methodInvoked</i> property to the <code>ClientRequestContext</code>
     * in a <code>ClientRequestFilter</code>. The user's <code>ClientRequestFilter</code>
     * should be able to read the return type, annotations, and parameters from
     * the <code>Method</code> from that property.
     *
     * @throws Exception - indicates test failure
     */
    @Test
    public void testRequestFilterReturnsMethodInvoked() throws Exception {
        ChildClient client = RestClientBuilder.newBuilder()
            .register(InvokedMethodRequestFilter.class)
            .baseUri(new URI("http://localhost:8080/neverUsed"))
            .build(ChildClient.class);

        Response response = client.executeBasePost();
        assertEquals(response.getStatus(), 200,
            "An exception occurred in the ClientRequestFilter");
        assertEquals(response.getHeaderString("ReturnType"), Response.class.getName());
        assertEquals(response.getHeaderString("POST"), "POST");
        assertEquals(response.getHeaderString("Path"), "/childOverride");
    }
}
