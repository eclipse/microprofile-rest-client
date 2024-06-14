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

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.ClientBuilderHeaderClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.ClientBuilderHeaderMethodClient;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithAllDuplicateClientHeadersFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public class ClientBuilderHeaderTest extends Arquillian {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, ClientBuilderHeaderTest.class.getSimpleName() + ".war")
                .addClasses(
                        ClientBuilderHeaderMethodClient.class,
                        ReturnWithAllDuplicateClientHeadersFilter.class,
                        WiremockArquillianTest.class);
    }

    @Test
    public void testHeaderBuilderMethod() {

        RestClientBuilder builder = RestClientBuilder.newBuilder().baseUri("http://localhost:8080/");
        builder.register(ReturnWithAllDuplicateClientHeadersFilter.class);
        builder.header("InterfaceAndBuilderHeader", "builder");
        ClientBuilderHeaderMethodClient client = builder.build(ClientBuilderHeaderMethodClient.class);

        checkHeaders(client.getAllHeaders("headerparam"), "method");
    }

    @Test
    public void testHeaderBuilderInterface() {

        RestClientBuilder builder = RestClientBuilder.newBuilder().baseUri("http://localhost:8080/");
        builder.register(ReturnWithAllDuplicateClientHeadersFilter.class);
        builder.header("InterfaceAndBuilderHeader", "builder");
        ClientBuilderHeaderClient client = builder.build(ClientBuilderHeaderClient.class);

        checkHeaders(client.getAllHeaders("headerparam"), "interface");
    }

    @Test
    public void testHeaderBuilderMethodNullValue() {

        RestClientBuilder builder = RestClientBuilder.newBuilder().baseUri("http://localhost:8080/");
        try {
            builder.header("BuilderHeader", null);
        } catch (NullPointerException npe) {
            return;
        }
        fail("header(\"builderHeader\", null) should have thrown a NullPointerException");
    }

    private static void checkHeaders(final JsonObject headers, final String clientHeaderParamName) {
        final List<String> clientRequestHeaders = headerValues(headers, "InterfaceAndBuilderHeader");

        assertTrue(clientRequestHeaders.contains("builder"),
                "Header InterfaceAndBuilderHeader did not container \"builder\": " + clientRequestHeaders);
        assertTrue(clientRequestHeaders.contains(clientHeaderParamName),
                "Header InterfaceAndBuilderHeader did not container \"" + clientHeaderParamName + "\": "
                        + clientRequestHeaders);

        final List<String> headerParamHeaders = headerValues(headers, "HeaderParam");
        assertTrue(headerParamHeaders.contains("headerparam"),
                "Header HeaderParam did not container \"headerparam\": " + headerParamHeaders);
    }

    private static List<String> headerValues(final JsonObject headers, final String headerName) {
        final JsonArray headerValues = headers.getJsonArray(headerName);
        Assert.assertNotNull(headerValues,
                String.format("Expected header '%s' to be present in %s", headerName, headers));
        return headerValues.stream().map(
                v -> (v.getValueType() == JsonValue.ValueType.STRING ? ((JsonString) v).getString() : v.toString()))
                .collect(Collectors.toList());
    }
}
