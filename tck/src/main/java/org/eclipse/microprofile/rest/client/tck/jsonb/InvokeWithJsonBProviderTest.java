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

package org.eclipse.microprofile.rest.client.tck.jsonb;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.assertEquals;

import java.time.LocalDate;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.WiremockArquillianTest;
import org.eclipse.microprofile.rest.client.tck.interfaces.JsonBClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.JsonBPrivateClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.MyJsonBContextResolver;
import org.eclipse.microprofile.rest.client.tck.interfaces.MyJsonBObject;
import org.eclipse.microprofile.rest.client.tck.interfaces.MyJsonBObjectWithPrivateProperties;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.SkipException;
import org.testng.annotations.Test;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

public class InvokeWithJsonBProviderTest extends WiremockArquillianTest {

    private static final String BASE_STUB_BODY = "{" +
            "\"objectName\": \"myObject\"," +
            "\"quantity\": 17," +
            "\"date\": \"2018-12-04\"" +
            "}";
    private static final String PRIVATE_STUB_BODY = "{" +
            "\"objectName\": \"myObject\"," +
            "\"quantity\": 17," +
            "\"date\": \"2018-12-04\"," +
            "\"privateObjectName\": \"myPrivateObject\"," +
            "\"privateQty\": 18" +
            "}";

    @Deployment
    public static WebArchive createDeployment() {
        StringAsset mpConfig = new StringAsset("jsonb/mp-rest/uri=" + getStringURL());
        return ShrinkWrap.create(WebArchive.class, InvokeWithJsonBProviderTest.class.getSimpleName() + ".war")
                .addClasses(JsonBClient.class, WiremockArquillianTest.class, MyJsonBObject.class,
                        JsonBPrivateClient.class,
                        MyJsonBContextResolver.class, MyJsonBObjectWithPrivateProperties.class,
                        InvokeWithJsonBProviderTest.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource(mpConfig, "classes/META-INF/microprofile-config.properties");
    }

    private static void assumeJsonbApiExists() throws SkipException {
        try {
            Class.forName("jakarta.json.bind.annotation.JsonbProperty");
        } catch (Throwable t) {
            throw new SkipException("Skipping since JSON-B APIs were not found.");
        }
    }

    @RestClient
    @Inject
    private JsonBClient cdiJsonBClient;

    @RestClient
    @Inject
    private Instance<JsonBPrivateClient> cdiJsonBPrivateClient;

    @Test
    public void testGetExecutesForBothClients() throws Exception {
        assumeJsonbApiExists();
        JsonBClient builtJsonBClient = RestClientBuilder.newBuilder().baseUri(getServerURI()).build(JsonBClient.class);
        setupStub("/myObject", BASE_STUB_BODY);
        MyJsonBObject obj = builtJsonBClient.get("myObject");
        testBaseGet(obj);

        setupStub("/myObject", BASE_STUB_BODY);
        obj = cdiJsonBClient.get("myObject");
        testBaseGet(obj);
    }

    @Test
    public void testCanSeePrivatePropertiesViaContextResolver() throws Exception {
        assumeJsonbApiExists();
        JsonBPrivateClient builtJsonBClient = RestClientBuilder.newBuilder().baseUri(getServerURI())
                .register(MyJsonBContextResolver.class)
                .build(JsonBPrivateClient.class);
        setupStub("/private/myObject", PRIVATE_STUB_BODY);
        MyJsonBObject obj = builtJsonBClient.getPrivate("myObject");
        testBaseGet(obj);
        assertEquals(obj.toString(), "PRIVATE_CTOR|myPrivateObject|18");

        setupStub("/private/myObject", PRIVATE_STUB_BODY);
        obj = cdiJsonBPrivateClient.get().getPrivate("myObject");
        testBaseGet(obj);
        assertEquals(obj.toString(), "PRIVATE_CTOR|myPrivateObject|18");
    }

    private void setupStub(String path, String body) throws Exception {
        reset();
        stubFor(get(urlEqualTo(path))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(body)));
    }

    private void testBaseGet(MyJsonBObject obj) throws Exception {
        assertEquals(obj.getName(), "myObject");
        assertEquals(obj.getQty(), 17);
        assertEquals(obj.getIgnoredField(), "CTOR");
        assertEquals(obj.getDate(), LocalDate.of(2018, 12, 04));
    }
}
