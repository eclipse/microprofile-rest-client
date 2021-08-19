/*
 * Copyright 2019, 2021 Contributors to the Eclipse Foundation
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.net.URI;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.ext.CustomClientHeadersFactory;
import org.eclipse.microprofile.rest.client.tck.interfaces.ClientHeadersFactoryClient;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithAllClientHeadersFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.json.JsonObject;

public class ClientHeadersFactoryTest extends Arquillian {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, ClientHeadersFactoryTest.class.getSimpleName() + ".war")
                .addClasses(ClientHeadersFactoryClient.class,
                        CustomClientHeadersFactory.class,
                        ReturnWithAllClientHeadersFilter.class);
    }

    private static ClientHeadersFactoryClient client(Class<?>... providers) {
        try {
            RestClientBuilder builder =
                    RestClientBuilder.newBuilder().baseUri(URI.create("http://localhost:9080/notused"));
            for (Class<?> provider : providers) {
                builder.register(provider);
            }
            return builder.build(ClientHeadersFactoryClient.class);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    @Test
    public void testClientHeadersFactoryInvoked() {
        CustomClientHeadersFactory.isIncomingHeadersMapNull = true;
        CustomClientHeadersFactory.isOutgoingHeadersMapNull = true;
        CustomClientHeadersFactory.passedInOutgoingHeaders.clear();

        JsonObject headers = client(ReturnWithAllClientHeadersFilter.class).delete("argValue");

        assertTrue(CustomClientHeadersFactory.invoked);
        assertFalse(CustomClientHeadersFactory.isIncomingHeadersMapNull);
        assertFalse(CustomClientHeadersFactory.isOutgoingHeadersMapNull);
        assertEquals(CustomClientHeadersFactory.passedInOutgoingHeaders.getFirst("IntfHeader"), "intfValue");
        assertEquals(CustomClientHeadersFactory.passedInOutgoingHeaders.getFirst("MethodHeader"), "methodValue");
        assertEquals(CustomClientHeadersFactory.passedInOutgoingHeaders.getFirst("ArgHeader"), "argValue");

        assertEquals(headers.getString("IntfHeader"), "intfValueModified");
        assertEquals(headers.getString("MethodHeader"), "methodValueModified");
        assertEquals(headers.getString("ArgHeader"), "argValueModified");
        assertEquals(headers.getString("FactoryHeader"), "factoryValue");
    }
}
