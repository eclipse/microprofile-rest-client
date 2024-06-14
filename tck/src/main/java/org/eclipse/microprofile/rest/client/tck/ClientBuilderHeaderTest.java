/*
 * Copyright 2024 Contributors to the Eclipse Foundation
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
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.ClientBuilderHeaderClient;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithAllClientHeadersFilter;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithAllDuplicateClientHeadersFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.github.tomakehurst.wiremock.client.MappingBuilder;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

public class ClientBuilderHeaderTest extends WiremockArquillianTest {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, ClientBuilderHeaderTest.class.getSimpleName() + ".war")
                .addClasses(
                        ClientBuilderHeaderClient.class,
                        ReturnWithAllDuplicateClientHeadersFilter.class,
                        WiremockArquillianTest.class);
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
    public void testHeaderBuilderMethod() {
        stub("InterfaceAndBuilderHeader", "builder", "interface", "method");

        RestClientBuilder builder = RestClientBuilder.newBuilder().baseUri(getServerURI());
        builder.register(ReturnWithAllClientHeadersFilter.class);
        builder.header("InterfaceAndBuilderHeader", "builder");
        ClientBuilderHeaderClient client = builder.build(ClientBuilderHeaderClient.class);

        JsonObject headers = client.getAllHeaders("headerparam");
        JsonArray header = headers.getJsonArray("InterfaceAndBuilderHeader");
        final List<String> headerValues =
                header.stream().map(v -> v.toString().toLowerCase()).collect(Collectors.toList());

        assertTrue(headerValues.contains("builder"),
                "Header InterfaceAndBuilderHeader did not container \"builder\": " + headers);
        assertTrue(headerValues.contains("interface"),
                "Header InterfaceAndBuilderHeader did not container \"interface\": " + headers);
        assertTrue(headerValues.contains("method"),
                "Header InterfaceAndBuilderHeader did not container \"method\": " + headers);
        assertTrue(headers.get("HeaderParam").toString().contains("headerparam"),
                "Header HeaderParam did not container \"headerparam\": " + headers);
    }

    @Test
    public void testHeaderBuilderMethodNullValue() {
        stub("BuilderHeader", "BuilderHeaderValue");

        RestClientBuilder builder = RestClientBuilder.newBuilder().baseUri(getServerURI());
        try {
            builder.header("BuilderHeader", null);
        } catch (NullPointerException npe) {
            return;
        }
        fail("header(\"builderHeader\", null) should have thrown a NullPointerException");
    }
}
