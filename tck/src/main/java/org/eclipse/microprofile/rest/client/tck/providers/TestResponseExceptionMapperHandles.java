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

package org.eclipse.microprofile.rest.client.tck.providers;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

@Priority(Priorities.USER + 1)
public class TestResponseExceptionMapperHandles implements ResponseExceptionMapper<Throwable> {
    private static boolean handlesCalled = false;
    private static boolean throwableCalled = false;
    @Override
    public Throwable toThrowable(Response response) {
        throwableCalled = true;
        return null;
    }

    @Override
    public boolean handles(int status, MultivaluedMap<String, Object> headers) {
        handlesCalled = true;
        return true;
    }

    public static void reset() {
        handlesCalled = false;
        throwableCalled = false;
    }

    public static boolean isHandlesCalled() {
        return handlesCalled;
    }

    public static boolean isThrowableCalled() {
        return throwableCalled;
    }
}
