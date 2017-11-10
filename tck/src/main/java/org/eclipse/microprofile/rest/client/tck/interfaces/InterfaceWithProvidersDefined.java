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

package org.eclipse.microprofile.rest.client.tck.interfaces;

import org.eclipse.microprofile.rest.client.annotation.RegisterProviders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientRequestFilter;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientResponseFilter;
import org.eclipse.microprofile.rest.client.tck.providers.TestMessageBodyReader;
import org.eclipse.microprofile.rest.client.tck.providers.TestMessageBodyWriter;
import org.eclipse.microprofile.rest.client.tck.providers.TestParamConverterProvider;
import org.eclipse.microprofile.rest.client.tck.providers.TestReaderInterceptor;
import org.eclipse.microprofile.rest.client.tck.providers.TestWriterInterceptor;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
@RegisterProviders({TestClientRequestFilter.class, TestClientResponseFilter.class, TestMessageBodyReader.class,
    TestMessageBodyWriter.class, TestParamConverterProvider.class, TestReaderInterceptor.class, TestWriterInterceptor.class})
@Produces(MediaType.TEXT_PLAIN)
@Consumes(MediaType.TEXT_PLAIN)
@RegisterRestClient
public interface InterfaceWithProvidersDefined {
    @POST
    Response executePost(String body);

    @PUT
    @Path("/{id}")
    Response executePut(@PathParam("id") String id, String body);
}
