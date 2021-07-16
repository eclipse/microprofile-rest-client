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

package org.eclipse.microprofile.rest.client.tck;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.JsonPClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.ws.rs.core.Response;

public class InvokeWithJsonPProviderTest extends WiremockArquillianTest {

    private static final String CDI = "cdi";
    private static final String BUILT = "built";

    @Deployment
    public static WebArchive createDeployment() {
        StringAsset mpConfig = new StringAsset(JsonPClient.class.getName() + "/mp-rest/url=" + getStringURL());
        return ShrinkWrap.create(WebArchive.class, InvokeWithJsonPProviderTest.class.getSimpleName() + ".war")
                .addClasses(JsonPClient.class, WiremockArquillianTest.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(mpConfig, "classes/META-INF/microprofile-config.properties");
    }

    @Inject
    @RestClient
    private JsonPClient cdiJsonPClient;

    private JsonPClient builtJsonPClient;

    public void setupClient() {
        builtJsonPClient = RestClientBuilder.newBuilder()
                .baseUri(getServerURI())
                .build(JsonPClient.class);
    }

    @Test
    public void testGetExecutesForBothClients() {
        setupClient();
        testGet(builtJsonPClient, BUILT);
        testGet(cdiJsonPClient, CDI);
    }

    @Test
    public void testGetSingleExecutesForBothClients() {
        setupClient();
        testGetSingle(builtJsonPClient, BUILT);
        testGetSingle(cdiJsonPClient, CDI);
    }

    @Test
    public void testPostExecutes() {
        setupClient();
        testPost(builtJsonPClient, BUILT);
        testPost(cdiJsonPClient, CDI);
    }

    @Test
    public void testPutExecutes() {
        setupClient();
        testPut(builtJsonPClient, BUILT);
        testPut(cdiJsonPClient, CDI);
    }

    private void testGet(JsonPClient client, String clientType) {
        reset();
        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"key\": \"value\"}, {\"key\": \"anotherValue\"}]")));
        JsonArray jsonArray = client.get();
        assertEquals(jsonArray.size(), 2, "Expected 2 values in the array for client " + clientType);
        List<JsonObject> jsonObjects = jsonArray.getValuesAs(JsonObject.class);
        JsonObject one = jsonObjects.get(0);
        assertEquals(one.keySet().size(), 1, "There should only be one key in object 1 for client " + clientType);
        assertEquals(one.getString("key"), "value",
                "The value of 'key' on object 1 should be 'value' in client " + clientType);

        JsonObject two = jsonObjects.get(1);
        assertEquals(two.keySet().size(), 1, "There should only be one key in object 2 for client " + clientType);
        assertEquals(two.getString("key"), "anotherValue",
                "The value of 'key' on object 2 should be 'anotherValue' in client " + clientType);

    }

    private void testGetSingle(JsonPClient client, String clientType) {
        reset();
        stubFor(get(urlEqualTo("/id"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"key\": \"value\"}")));
        JsonObject jsonObject = client.get("id");
        assertEquals(jsonObject.keySet().size(), 1, "There should only be one key in object for client " + clientType);
        assertEquals(jsonObject.getString("key"), "value",
                "The value of 'key' on object should be 'value' in client " + clientType);

    }

    private void testPost(JsonPClient client, String clientType) {
        reset();
        stubFor(post(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)));

        JsonObject jsonObject = Json.createObjectBuilder().add("someKey", "newValue").build();
        String jsonObjectAsString = jsonObject.toString();
        Response response = client.post(jsonObject);
        response.close();
        assertEquals(response.getStatus(), 200, "Expected a 200 OK on client " + clientType);

        verify(1, postRequestedFor(urlEqualTo("/")).withRequestBody(equalTo(jsonObjectAsString)));
    }

    private void testPut(JsonPClient client, String clientType) {
        reset();
        stubFor(put(urlEqualTo("/id"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200).withBody("{\"someOtherKey\":\"newValue\"}")));

        JsonObject jsonObject = Json.createObjectBuilder().add("someKey", "newValue").build();
        String jsonObjectAsString = jsonObject.toString();
        JsonObject response = client.update("id", jsonObject);
        assertEquals(response.getString("someOtherKey"), "newValue",
                "The value of 'someOtherKey' on response should be 'someOtherKey' in client " + clientType);

        verify(1, putRequestedFor(urlEqualTo("/id")).withRequestBody(equalTo(jsonObjectAsString)));
    }
}
