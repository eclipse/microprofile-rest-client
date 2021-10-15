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

package org.eclipse.microprofile.rest.client.tck.providers;

import java.io.IOException;
import java.lang.reflect.Method;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Response;

public class InvokedMethodRequestFilter implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext ctx) throws IOException {
        try {
            Method m = (Method) ctx.getProperty("org.eclipse.microprofile.rest.client.invokedMethod");

            Path path = m.getAnnotation(Path.class);
            ctx.abortWith(Response.ok("OK")
                    .header("ReturnType", m.getReturnType().getName())
                    .header("POST", m.getAnnotation(POST.class) == null ? "null" : "POST")
                    .header("Path", path == null ? "null" : path.value())
                    .build());
        } catch (Throwable t) {
            t.printStackTrace();
            ctx.abortWith(Response.serverError().build());
        }
    }
}
