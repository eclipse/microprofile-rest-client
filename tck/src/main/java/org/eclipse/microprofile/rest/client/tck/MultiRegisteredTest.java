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
import org.eclipse.microprofile.rest.client.tck.interfaces.InterfaceWithPriority;
import org.eclipse.microprofile.rest.client.tck.interfaces.InterfaceWithoutPriority;
import org.eclipse.microprofile.rest.client.tck.providers.InjectedSimpleFeature;
import org.eclipse.microprofile.rest.client.tck.providers.Prioritized2000MessageBodyReader;
import org.eclipse.microprofile.rest.client.tck.providers.UnprioritizedMessageBodyReader;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class MultiRegisteredTest extends WiremockArquillianTest {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClasses(InterfaceWithoutPriority.class, InterfaceWithPriority.class)
            .addPackage(InjectedSimpleFeature.class.getPackage());
    }

    @Test
    public void testOverrideProviderAnnotationOnBuilder() {
        InterfaceWithoutPriority client = RestClientBuilder.newBuilder().register(UnprioritizedMessageBodyReader.class, 1000)
            .register(Prioritized2000MessageBodyReader.class, 500)
            .baseUrl(getServerURL())
            .build(InterfaceWithoutPriority.class);
        String body = client.get().readEntity(String.class);
        assertEquals(body, "Prioritized 2000", "The body returned should be the body from "+Prioritized2000MessageBodyReader.class);
    }

    @Test
    public void testOverrideInterfaceAndProviderAnnotationOnBuilder() {
        InterfaceWithPriority client = RestClientBuilder.newBuilder().register(UnprioritizedMessageBodyReader.class, 1000)
            .register(Prioritized2000MessageBodyReader.class, 500)
            .baseUrl(getServerURL())
            .build(InterfaceWithPriority.class);
        String body = client.get().readEntity(String.class);
        assertEquals(body, "Prioritized 2000", "The body returned should be the body from "+Prioritized2000MessageBodyReader.class);
    }
}
