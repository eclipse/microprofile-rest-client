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

package org.eclipse.microprofile.rest.client.tck;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.InterfaceWithProvidersDefined;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientRequestFilter;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientResponseFilter;
import org.eclipse.microprofile.rest.client.tck.providers.TestMessageBodyReader;
import org.eclipse.microprofile.rest.client.tck.providers.TestReaderInterceptor;
import org.eclipse.microprofile.rest.client.tck.providers.TestWriterInterceptor;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.testng.Assert.assertEquals;

public class InvokeWithRegisteredProvidersTest extends WiremockArquillianTest {
    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .addClass(InterfaceWithProvidersDefined.class)
            .addPackage(TestClientResponseFilter.class.getPackage());
    }

    @Test
    public void testInvokesPostOperationWithAnnotatedProviders() throws Exception{
        String inputBody = "input body will be removed";
        String outputBody = "output body will be removed";
        String expectedReceivedBody = "this is the replaced writer "+inputBody;
        String expectedResponseBody = TestMessageBodyReader.REPLACED_BODY;
        stubFor(post(urlEqualTo("/"))
            .willReturn(aResponse()
                .withBody(outputBody)));

        InterfaceWithProvidersDefined api = RestClientBuilder.newBuilder()
            .baseUrl(getServerURL())
            .build(InterfaceWithProvidersDefined.class);

        Response response = api.executePost(inputBody);

        String body = response.readEntity(String.class);

        response.close();

        assertEquals(body, expectedResponseBody);

        verify(1, postRequestedFor(urlEqualTo("/")).withRequestBody(equalTo(expectedReceivedBody)));

        assertEquals(TestClientResponseFilter.getAndResetValue(),1);
        assertEquals(TestClientRequestFilter.getAndResetValue(),1);
        assertEquals(TestReaderInterceptor.getAndResetValue(),1);
        assertEquals(TestWriterInterceptor.getAndResetValue(),1);
    }

    @Test
    public void testInvokesPutOperationWithAnnotatedProviders() throws Exception {
        String inputBody = "input body will be removed";
        String outputBody = "output body will be removed";
        String expectedReceivedBody = "this is the replaced writer "+inputBody;
        String expectedResponseBody = TestMessageBodyReader.REPLACED_BODY;
        String id = "id";
        String expectedId = "toStringid";
        stubFor(put(urlEqualTo("/"+expectedId))
            .willReturn(aResponse()
                .withBody(outputBody)));

        InterfaceWithProvidersDefined api = RestClientBuilder.newBuilder()
            .baseUrl(getServerURL())
            .build(InterfaceWithProvidersDefined.class);

        Response response = api.executePut(id, inputBody);

        String body = response.readEntity(String.class);

        response.close();

        assertEquals(body, expectedResponseBody);

        verify(1, putRequestedFor(urlEqualTo("/"+expectedId)).withRequestBody(equalTo(expectedReceivedBody)));

        assertEquals(TestClientResponseFilter.getAndResetValue(),1);
        assertEquals(TestClientRequestFilter.getAndResetValue(),1);
        assertEquals(TestReaderInterceptor.getAndResetValue(),1);
        assertEquals(TestWriterInterceptor.getAndResetValue(),1);
    }
}
