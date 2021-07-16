/*
 * Copyright 2018, 2021 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.tck.interfaces;

import java.util.Date;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.core.Response;

public interface InvalidComputeMethodSignature {

    default String invalidMethod(String headerName, String someOtherArg) {
        return "should not be invoked - too many String args";
    }

    default String invalidMethod(String headerName, Integer someOtherArg) {
        return "should not be invoked - unexpected Integer arg";
    }

    default String invalidMethod(ClientRequestContext context, ClientRequestContext someOtherArg) {
        return "should not be invoked - too many ClientRequestContext args";
    }

    default String invalidMethod(ClientRequestContext context, Date someOtherArg) {
        return "should not be invoked - unexpected Date arg";
    }

    default String invalidMethod(Double unexpectedArg) {
        return "should not be invoked - unexpected Double arg";
    }

    @ClientHeaderParam(name = "TestHeader", value = "{invalidMethod}")
    @GET
    Response get();
}
