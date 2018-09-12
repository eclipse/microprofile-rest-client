/*
 * Copyright 2018 Contributors to the Eclipse Foundation
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

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

/**
 * Aborts with a response showing "Sent-Accept" and "Sent-ContentType" headers
 * to indicate the request's header values for "Accept" and "Content-type",
 * respectively.
 */
public class ProducesConsumesFilter implements ClientRequestFilter {
    @Override
    public void filter(ClientRequestContext ctx) throws IOException {
        String contentType = ctx.getHeaderString(HttpHeaders.CONTENT_TYPE);
        String accept = ctx.getHeaderString(HttpHeaders.ACCEPT);
        ctx.abortWith(Response.ok()
                              .header("Sent-Accept", accept)
                              .header("Sent-ContentType", contentType)
                              .build());

    }

}
