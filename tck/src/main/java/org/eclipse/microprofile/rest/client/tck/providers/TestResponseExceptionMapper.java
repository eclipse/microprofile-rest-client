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

package org.eclipse.microprofile.rest.client.tck.providers;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Priority(Priorities.USER + 2)
public class TestResponseExceptionMapper implements ResponseExceptionMapper<Throwable> {
    public static final String MESSAGE = "A 200 OK was received, but I'm throwing an exception";
    private static boolean handlesCalled = false;
    private static boolean throwableCalled = false;
    @Override
    public Throwable toThrowable(Response response) {
        throwableCalled = true;
        return new WebApplicationException(MESSAGE);
    }

    @Override
    public boolean handles(Response response) {
        handlesCalled = true;
        return response.getStatus() == 200;
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
