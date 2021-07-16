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

import static jakarta.ws.rs.Priorities.ENTITY_CODER;
import static org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper.DEFAULT_PRIORITY;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import jakarta.annotation.Priority;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

public class ResponseExceptionMapperTest {

    @Test
    public void testHandles() {
        final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        final DummyResponseExceptionMapper mapper = new DummyResponseExceptionMapper();
        assertTrue(mapper.handles(500, headers));
        assertTrue(mapper.handles(400, headers));
        assertFalse(mapper.handles(300, headers));
    }

    @Test
    public void testGetPriority() {
        final DummyResponseExceptionMapper mapper = new DummyResponseExceptionMapper();
        assertEquals(DEFAULT_PRIORITY, mapper.getPriority());
    }

    @Test
    public void testGetPriorityWithAnnotation() {
        final PriorityResponseExceptionMapper mapper = new PriorityResponseExceptionMapper();
        assertEquals(ENTITY_CODER, mapper.getPriority());
    }

    private static class DummyResponseExceptionMapper implements ResponseExceptionMapper<WebApplicationException> {
        @Override
        public WebApplicationException toThrowable(Response response) {
            return new WebApplicationException(response);
        }
    }

    @Priority(ENTITY_CODER)
    private static class PriorityResponseExceptionMapper implements ResponseExceptionMapper<WebApplicationException> {
        @Override
        public WebApplicationException toThrowable(Response response) {
            return new WebApplicationException(response);
        }
    }
}
