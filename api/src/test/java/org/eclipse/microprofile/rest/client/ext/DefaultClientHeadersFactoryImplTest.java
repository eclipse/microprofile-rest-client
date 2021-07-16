/*
 * Copyright (c) 2019, 2021 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.ext;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

public class DefaultClientHeadersFactoryImplTest {

    private DefaultClientHeadersFactoryImpl impl = new DefaultClientHeadersFactoryImpl();

    @BeforeMethod
    public void unsetPropagationProperty() {
        System.clearProperty(DefaultClientHeadersFactoryImpl.PROPAGATE_PROPERTY);
    }

    private MultivaluedMap<String, String> mockIncomingHeaders() {
        MultivaluedMap<String, String> incomingHeaders = new MultivaluedHashMap<>();
        incomingHeaders.putSingle("Authorization", "Basic xyz123");
        incomingHeaders.putSingle("Content-Type", "application/json");
        incomingHeaders.putSingle("Accept", "application/json");
        incomingHeaders.putSingle("Favorite-Color", "blue");
        return incomingHeaders;
    }

    private MultivaluedMap<String, String> mockOutgoingHeaders() {
        MultivaluedMap<String, String> outgoingHeaders = new MultivaluedHashMap<>();
        outgoingHeaders.putSingle("Custom-Header", "my custom value");
        return outgoingHeaders;
    }

    @Test
    public void testUpdateWithNoConfigReturnsEmptyMap() {
        MultivaluedMap<String, String> incomingHeaders = mockIncomingHeaders();
        MultivaluedMap<String, String> clientOutgoingHeaders = new MultivaluedHashMap<>();
        MultivaluedMap<String, String> updatedHeaders = impl.update(incomingHeaders, clientOutgoingHeaders);
        assertNotNull(updatedHeaders);
        assertEquals(updatedHeaders.size(), 0);
    }

    @Test
    public void testUpdateWithConfiguredPropagationHeaders() {
        System.setProperty(DefaultClientHeadersFactoryImpl.PROPAGATE_PROPERTY, "Authorization,Favorite-Color");
        MultivaluedMap<String, String> incomingHeaders = mockIncomingHeaders();
        MultivaluedMap<String, String> clientOutgoingHeaders = new MultivaluedHashMap<>();
        MultivaluedMap<String, String> updatedHeaders = impl.update(incomingHeaders, clientOutgoingHeaders);
        assertNotNull(updatedHeaders);
        assertEquals(updatedHeaders.size(), 2);
        assertEquals("Basic xyz123", updatedHeaders.getFirst("Authorization"));
        assertEquals("blue", updatedHeaders.getFirst("Favorite-Color"));
    }

    @Test
    public void testUpdateWithConfiguredPropagationHeadersAndExistingOutgoingHeaders() {
        System.setProperty(DefaultClientHeadersFactoryImpl.PROPAGATE_PROPERTY, "Authorization,Favorite-Color");
        MultivaluedMap<String, String> incomingHeaders = mockIncomingHeaders();
        MultivaluedMap<String, String> clientOutgoingHeaders = mockOutgoingHeaders();
        MultivaluedMap<String, String> updatedHeaders = impl.update(incomingHeaders, clientOutgoingHeaders);
        assertNotNull(updatedHeaders);
        assertEquals(updatedHeaders.size(), 2);
        assertEquals("Basic xyz123", updatedHeaders.getFirst("Authorization"));
        assertEquals("blue", updatedHeaders.getFirst("Favorite-Color"));
    }
}
