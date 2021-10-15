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

package org.eclipse.microprofile.rest.client.tck.cditests;

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

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.WiremockArquillianTest;
import org.eclipse.microprofile.rest.client.tck.interfaces.InterfaceBase;
import org.eclipse.microprofile.rest.client.tck.interfaces.InterfaceWithProvidersDefined;
import org.eclipse.microprofile.rest.client.tck.interfaces.InterfaceWithoutProvidersDefined;
import org.eclipse.microprofile.rest.client.tck.interfaces.InterfaceWithoutProvidersDefinedWithConfigKey;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientRequestFilter;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientResponseFilter;
import org.eclipse.microprofile.rest.client.tck.providers.TestMessageBodyReader;
import org.eclipse.microprofile.rest.client.tck.providers.TestMessageBodyWriter;
import org.eclipse.microprofile.rest.client.tck.providers.TestParamConverterProvider;
import org.eclipse.microprofile.rest.client.tck.providers.TestReaderInterceptor;
import org.eclipse.microprofile.rest.client.tck.providers.TestWriterInterceptor;
import org.eclipse.microprofile.rest.client.tck.providers.Widget;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

/**
 * Verifies via CDI injection that you can use a programmatic interface. Verifies that the interface includes registered
 * providers. Also verifies that providers registered via MicroProfile Config are honored.
 */
public class CDIInvokeWithRegisteredProvidersTest extends WiremockArquillianTest {
    @Inject
    @RestClient
    private InterfaceWithProvidersDefined clientProvidersViaAnnotation;

    @Inject
    @RestClient
    private InterfaceWithoutProvidersDefined clientProvidersViaMPConfig;

    @Inject
    @RestClient
    private InterfaceWithoutProvidersDefinedWithConfigKey clientProvidersViaConfigKey;

    @Deployment
    public static WebArchive createDeployment() {
        String urlPropName1 = InterfaceWithProvidersDefined.class.getName() + "/mp-rest/url";
        String urlPropName2 = InterfaceWithoutProvidersDefined.class.getName() + "/mp-rest/url";
        String urlPropName3 = "theKey/mp-rest/url";
        String urlValue = getStringURL();
        String simpleName = CDIInvokeWithRegisteredProvidersTest.class.getSimpleName();
        String providersPropName = InterfaceWithoutProvidersDefined.class.getName() + "/mp-rest/providers";
        String providersValue = TestClientRequestFilter.class.getName() + "," +
                TestClientResponseFilter.class.getName() + "," +
                TestMessageBodyReader.class.getName() + "," +
                TestMessageBodyWriter.class.getName() + "," +
                TestParamConverterProvider.class.getName() + "," +
                TestReaderInterceptor.class.getName() + "," +
                TestWriterInterceptor.class.getName();
        String providersConfigKeyPropName = "theKey/mp-rest/providers";
        String propsFile = String.format(urlPropName1 + "=" + urlValue + "%n" +
                urlPropName2 + "=" + urlValue + "%n" +
                urlPropName3 + "=" + urlValue + "%n" +
                providersPropName + "=" + providersValue + "%n" +
                providersConfigKeyPropName + "=" + providersValue);
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, simpleName + ".jar")
                .addClasses(InterfaceWithProvidersDefined.class,
                        InterfaceWithoutProvidersDefined.class,
                        InterfaceWithoutProvidersDefinedWithConfigKey.class,
                        InterfaceBase.class,
                        WiremockArquillianTest.class)
                .addPackage(TestClientResponseFilter.class.getPackage())
                .addAsManifestResource(new StringAsset(propsFile), "microprofile-config.properties")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
                .addAsLibrary(jar)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testInvokesPostOperation_viaAnnotation() throws Exception {
        testInvokesPostOperation(clientProvidersViaAnnotation);
    }

    @Test
    public void testInvokesPostOperation_viaMPConfig() throws Exception {
        testInvokesPostOperation(clientProvidersViaMPConfig);
    }

    @Test
    public void testInvokesPostOperation_viaMPConfigWithConfigKey() throws Exception {
        testInvokesPostOperation(clientProvidersViaConfigKey);
    }

    private void testInvokesPostOperation(InterfaceBase api) throws Exception {
        String inputBody = "input body will be removed";
        String outputBody = "output body will be removed";
        String expectedReceivedBody = "this is the replaced writer " + inputBody;
        String expectedResponseBody = TestMessageBodyReader.REPLACED_BODY;
        stubFor(post(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withBody(outputBody)));

        Response response = api.executePost(inputBody);

        String body = response.readEntity(String.class);

        response.close();

        assertEquals(body, expectedResponseBody);

        verify(postRequestedFor(urlEqualTo("/")).withRequestBody(equalTo(expectedReceivedBody)));

        assertEquals(TestClientResponseFilter.getAndResetValue(), 1);
        assertEquals(TestClientRequestFilter.getAndResetValue(), 1);
        assertEquals(TestReaderInterceptor.getAndResetValue(), 1);
        assertEquals(TestWriterInterceptor.getAndResetValue(), 1);
    }

    @Test
    public void testInvokesPutOperation_viaAnnotation() throws Exception {
        testInvokesPutOperation(clientProvidersViaAnnotation);
    }

    @Test
    public void testInvokesPutOperation_viaMPConfig() throws Exception {
        testInvokesPutOperation(clientProvidersViaMPConfig);
    }

    @Test
    public void testInvokesPutOperation_viaMPConfigWithConfigKey() throws Exception {
        testInvokesPutOperation(clientProvidersViaConfigKey);
    }

    private void testInvokesPutOperation(InterfaceBase api) throws Exception {
        String inputBody = "input body will be removed";
        String outputBody = "output body will be removed";
        String expectedReceivedBody = "this is the replaced writer " + inputBody;
        String expectedResponseBody = TestMessageBodyReader.REPLACED_BODY;
        Widget id = new Widget("MyWidget", 7);
        String expectedId = "MyWidget:7";
        stubFor(put(urlEqualTo("/" + expectedId))
                .willReturn(aResponse()
                        .withBody(outputBody)));

        Response response = api.executePut(id, inputBody);

        String body = response.readEntity(String.class);

        response.close();

        assertEquals(body, expectedResponseBody);

        verify(putRequestedFor(urlEqualTo("/" + expectedId)).withRequestBody(equalTo(expectedReceivedBody)));

        assertEquals(TestClientResponseFilter.getAndResetValue(), 1);
        assertEquals(TestClientRequestFilter.getAndResetValue(), 1);
        assertEquals(TestReaderInterceptor.getAndResetValue(), 1);
        assertEquals(TestWriterInterceptor.getAndResetValue(), 1);
    }
}
