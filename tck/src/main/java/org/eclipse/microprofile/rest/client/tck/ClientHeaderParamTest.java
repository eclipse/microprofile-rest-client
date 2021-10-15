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

package org.eclipse.microprofile.rest.client.tck;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.ext.HeaderGenerator;
import org.eclipse.microprofile.rest.client.tck.interfaces.ClientHeaderParamClient;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithAllClientHeadersFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.github.tomakehurst.wiremock.client.MappingBuilder;

import jakarta.json.JsonObject;

public class ClientHeaderParamTest extends WiremockArquillianTest {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, ClientHeaderParamTest.class.getSimpleName() + ".war")
                .addClasses(
                        ClientHeaderParamClient.class,
                        ReturnWithAllClientHeadersFilter.class,
                        HeaderGenerator.class,
                        WiremockArquillianTest.class);
    }

    private static ClientHeaderParamClient client(Class<?>... providers) {
        try {
            RestClientBuilder builder = RestClientBuilder.newBuilder().baseUri(getServerURI());
            for (Class<?> provider : providers) {
                builder.register(provider);
            }
            return builder.build(ClientHeaderParamClient.class);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    private static void stub(String expectedHeaderName, String... expectedHeaderValue) {
        String expectedIncomingHeader = Arrays.stream(expectedHeaderValue)
                .collect(Collectors.joining(","));
        String outputBody = expectedIncomingHeader.replace(',', '-');
        MappingBuilder mappingBuilder = get(urlEqualTo("/"));

        // headers can be sent either in a single line with comma-separated values or in multiple lines
        // this should match both cases:
        Arrays.stream(expectedHeaderValue)
                .forEach(val -> mappingBuilder.withHeader(expectedHeaderName, containing(val)));
        stubFor(
                mappingBuilder
                        .willReturn(
                                aResponse().withStatus(200)
                                        .withBody(outputBody)));
    }
    @BeforeTest
    public void resetWiremock() {
        setupServer();
    }

    @Test
    public void testExplicitClientHeaderParamOnInterface() {
        stub("InterfaceHeaderExplicit", "interfaceExplicit");
        assertEquals(client().interfaceExplicit(), "interfaceExplicit");
    }
    @Test
    public void testExplicitClientHeaderParamOnMethod() {
        stub("MethodHeaderExplicit", "methodExplicit");
        assertEquals(client().methodExplicit(), "methodExplicit");
    }

    @Test
    public void testHeaderParamOverridesExplicitClientHeaderParamOnInterface() {
        stub("InterfaceHeaderExplicit", "header");
        assertEquals(client().headerParamOverridesInterfaceExplicit("header"), "header");
    }

    @Test
    public void testHeaderParamOverridesExplicitClientHeaderParamOnMethod() {
        stub("MethodHeaderExplicit", "header2");
        assertEquals(client().headerParamOverridesMethodExplicit("header2"), "header2");
    }

    @Test
    public void testExplicitClientHeaderParamOnMethodOverridesClientHeaderParamOnInterface() {
        stub("OverrideableExplicit", "overriddenMethodExplicit");
        assertEquals(client().methodClientHeaderParamOverridesInterfaceExplicit(),
                "overriddenMethodExplicit");
    }

    @Test
    public void testComputedClientHeaderParamOnInterface() {
        stub("InterfaceHeaderComputed", "interfaceComputed");
        assertEquals(client().interfaceComputed(), "interfaceComputed");
    }

    @Test
    public void testComputedClientHeaderParamOnMethod() {
        stub("MethodHeaderComputed", "MethodHeaderComputed-X");
        assertEquals(client().methodComputed(), "MethodHeaderComputed-X");
    }

    @Test
    public void testHeaderParamOverridesComputedClientHeaderParamOnInterface() {
        stub("InterfaceHeaderComputed", "override");
        assertEquals(client().headerParamOverridesInterfaceComputed("override"), "override");
    }

    @Test
    public void testHeaderParamOverridesComputedClientHeaderParamOnMethod() {
        stub("MethodHeaderComputed", "override2");
        assertEquals(client().headerParamOverridesMethodComputed("override2"), "override2");
    }

    @Test
    public void testComputedClientHeaderParamOnMethodOverridesClientHeaderParamOnInterface() {
        stub("OverrideableComputed", "overriddenMethodComputed");
        assertEquals(client().methodClientHeaderParamOverridesInterfaceComputed(),
                "overriddenMethodComputed");
    }

    @Test
    public void testExceptionInRequiredComputeMethodThrowsClientErrorException() {
        try {
            client().methodRequiredComputeMethodFails();
            fail("Expected exception to be thrown");
        } catch (Throwable t) {
            if (t instanceof RuntimeException) {
                assertEquals(t.getMessage(), "intentional");
            } else {
                fail("Threw unexpected exception, " + t + ", expected RuntimeException");
            }
        }
    }

    @Test
    public void testHeaderNotSentWhenExceptionThrownAndRequiredIsFalse() {
        JsonObject headers = client(ReturnWithAllClientHeadersFilter.class)
                .methodOptionalMethodHeaderNotSentWhenComputeThrowsException();

        assertFalse(headers.containsKey("OptionalInterfaceHeader"));
        assertFalse(headers.containsKey("OptionalMethodHeader"));

        // sanity check that the filter did return _some_ headers
        assertEquals(headers.getString("OverrideableExplicit"), "overrideableInterfaceExplicit");
        assertEquals(headers.getString("InterfaceHeaderComputed"), "interfaceComputed");
        assertEquals(headers.getString("MethodHeaderExplicit"), "SomeValue");
    }

    @Test
    public void testMultivaluedHeaderSentWhenInvokingComputeMethodFromSeparateClass() {
        stub("MultiValueInvokedFromAnotherClass", "value1", "value2");
        assertEquals(client().methodComputeMultiValuedHeaderFromOtherClass(),
                "value1-value2");
    }

    @Test
    public void testMultivaluedHeaderInterfaceExplicit() {
        stub("InterfaceMultiValuedHeaderExplicit", "abc", "xyz");
        assertEquals(client().methodComputeMultiValuedHeaderFromOtherClass(),
                "abc-xyz");
    }
}
