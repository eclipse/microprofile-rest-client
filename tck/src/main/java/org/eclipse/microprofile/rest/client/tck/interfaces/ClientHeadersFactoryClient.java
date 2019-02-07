/*
 * Copyright 2019 Contributors to the Eclipse Foundation
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

import javax.json.JsonObject;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.tck.ext.CustomClientHeadersFactory;


@Path("/")
@RegisterClientHeaders(CustomClientHeadersFactory.class)
@ClientHeaderParam(name="IntfHeader", value="intfValue")
public interface ClientHeadersFactoryClient {
    @DELETE
    @ClientHeaderParam(name="MethodHeader", value="methodValue")
    JsonObject delete(@HeaderParam("ArgHeader") String argHeader);
}
