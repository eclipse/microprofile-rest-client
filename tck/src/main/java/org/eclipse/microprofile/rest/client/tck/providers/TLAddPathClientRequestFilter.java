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
import java.net.URI;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.UriBuilder;

public class TLAddPathClientRequestFilter implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        URI updatedUri = UriBuilder.fromUri(clientRequestContext.getUri())
                                   .path("/" + TLAsyncInvocationInterceptorFactory.getTlInt())
                                   .build();
        clientRequestContext.setUri(updatedUri);
    }
}
