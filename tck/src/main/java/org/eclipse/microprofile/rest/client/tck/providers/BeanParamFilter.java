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

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class BeanParamFilter implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        String body = (String)clientRequestContext.getEntity();
        String query = clientRequestContext.getUri().getQuery();
        Cookie cookie = clientRequestContext.getCookies().get("cookie");
        String cookieValue = cookie==null?"null":cookie.getValue();
        String header = clientRequestContext.getHeaderString("MyHeader");
        clientRequestContext.abortWith(Response.ok(query + " " + cookieValue + " " + header + " " + body).build());
    }
}
