/*
 * Copyright 2024 Contributors to the Eclipse Foundation
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

import java.net.URI;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.BaseClient;
import org.eclipse.microprofile.rest.client.tck.providers.PriorityResult;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientRequestFilterPriority1;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientRequestFilterPriority2;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientRequestFilterPriority3;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientRequestFilterPriority4;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientResponseFilterPriority1;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientResponseFilterPriority2;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientResponseFilterPriority3;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientResponseFilterPriority4;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.Response;

public class FilterPriorityTest extends Arquillian {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, FilterPriorityTest.class.getSimpleName() + ".war")
                .addPackage(TestClientRequestFilterPriority1.class.getPackage())
                .addClasses(BaseClient.class);
    }

    @Test
    public void testPriorities() throws Exception {
        TestClientRequestFilterPriority1 filter1 = new TestClientRequestFilterPriority1();
        TestClientRequestFilterPriority2 filter2 = new TestClientRequestFilterPriority2();
        TestClientRequestFilterPriority3 filter3 = new TestClientRequestFilterPriority3();
        TestClientRequestFilterPriority4 filter4 = new TestClientRequestFilterPriority4();
        TestClientResponseFilterPriority1 filter5 = new TestClientResponseFilterPriority1();
        TestClientResponseFilterPriority2 filter6 = new TestClientResponseFilterPriority2();
        TestClientResponseFilterPriority3 filter7 = new TestClientResponseFilterPriority3();
        TestClientResponseFilterPriority4 filter8 = new TestClientResponseFilterPriority4();

        RestClientBuilder builder = RestClientBuilder.newBuilder()
                .register(filter1, 3000)
                .register(filter2, 1000)
                .register(filter3, 4000)
                .register(filter4, 2000)
                .register(filter5, 2000)
                .register(filter6, 4000)
                .register(filter7, 1000)
                .register(filter8, 3000);
        BaseClient client = builder.baseUri(new URI("http://localhost/stub")).build(BaseClient.class);

        Response response = client.executeBasePost();
        assertEquals(response.getStatus(), 200,
                "An exception occurred in the ClientRequestFilter");
        PriorityResult result = PriorityResult.getResult();
        // 4 filters specified in MP Config
        assertEquals(4, result.requestsInvoked.size());

        // priority order specified in same place as filters themselves
        assertEquals("TestClientRequestFilterPriority2", result.requestsInvoked.get(0).getSimpleName());
        assertEquals("TestClientRequestFilterPriority4", result.requestsInvoked.get(1).getSimpleName());
        assertEquals("TestClientRequestFilterPriority1", result.requestsInvoked.get(2).getSimpleName());
        assertEquals("TestClientRequestFilterPriority3", result.requestsInvoked.get(3).getSimpleName());
        // Per the Jakarta Rest specification, the order of priorities between Client
        // request and response filters are reversed.
        assertEquals("TestClientResponseFilterPriority2", result.responsesInvoked.get(0).getSimpleName());
        assertEquals("TestClientResponseFilterPriority4", result.responsesInvoked.get(1).getSimpleName());
        assertEquals("TestClientResponseFilterPriority1", result.responsesInvoked.get(2).getSimpleName());
        assertEquals("TestClientResponseFilterPriority3", result.responsesInvoked.get(3).getSimpleName());
    }
}
