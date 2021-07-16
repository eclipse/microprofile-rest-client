/*
 * Copyright (c) 2020, 2021 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.microprofile.rest.client.tck;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApiWithConfigKey;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.github.tomakehurst.wiremock.client.WireMock;

import jakarta.ws.rs.core.Response;

public class FollowRedirectsTest extends WiremockArquillianTest {
    private static final String FOLLOWED_REDIRECT = "followed redirect";
    private static final String DID_NOT_FOLLOW_REDIRECT = "did not follow redirect";
    private static final String LOCATION = "Location";

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class,
                FollowRedirectsTest.class.getSimpleName() + ".war")
                .addClasses(SimpleGetApi.class, SimpleGetApiWithConfigKey.class, WiremockArquillianTest.class);
    }

    @BeforeMethod
    public void reset() {
        WireMock.reset();
    }

    @Test
    public void test301Default() throws Exception {
        testDefault(301, getDefaultClientInstance());
    }

    @Test
    public void test301Follows() throws Exception {
        testFollows(301, getConfiguredClientInstance());
    }

    @Test
    public void test302Default() throws Exception {
        testDefault(302, getDefaultClientInstance());
    }

    @Test
    public void test302Follows() throws Exception {
        testFollows(302, getConfiguredClientInstance());
    }

    @Test
    public void test303Default() throws Exception {
        testDefault(303, getDefaultClientInstance());
    }

    @Test
    public void test303Follows() throws Exception {
        testFollows(303, getConfiguredClientInstance());
    }

    @Test
    public void test307Default() throws Exception {
        testDefault(307, getDefaultClientInstance());
    }

    @Test
    public void test307Follows() throws Exception {
        testFollows(307, getConfiguredClientInstance());
    }

    protected SimpleGetApi getDefaultClientInstance() {
        return RestClientBuilder.newBuilder().baseUri(getServerURI()).build(SimpleGetApi.class);
    }

    protected SimpleGetApiWithConfigKey getConfiguredClientInstance() {
        return RestClientBuilder.newBuilder().baseUri(getServerURI()).followRedirects(true)
                .build(SimpleGetApiWithConfigKey.class);
    }

    public static void testDefault(int redirectCode, SimpleGetApi defaultClient) throws Exception {
        try (Response response = execute(defaultClient, redirectCode)) {
            assertEquals(response.getStatus(), redirectCode);
            assertEquals(response.getHeaderString(LOCATION), getStringURL() + "redirected");
            assertEquals(response.readEntity(String.class), DID_NOT_FOLLOW_REDIRECT);

            verify(1, getRequestedFor(urlEqualTo("/")));
            verify(0, getRequestedFor(urlEqualTo("/redirected")));
        }
    }

    public static void testFollows(int redirectCode, SimpleGetApi followingClient) throws Exception {
        try (Response response = execute(followingClient, redirectCode)) {
            assertEquals(response.getStatus(), 200);
            assertNull(response.getHeaderString(LOCATION));
            assertEquals(response.readEntity(String.class), FOLLOWED_REDIRECT);

            verify(1, getRequestedFor(urlEqualTo("/")));
            verify(1, getRequestedFor(urlEqualTo("/redirected")));
        }
    }

    private static Response execute(SimpleGetApi simpleGetApi, int redirectCode) throws Exception {
        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withStatus(redirectCode)
                        .withBody(DID_NOT_FOLLOW_REDIRECT)
                        .withHeader(LOCATION, getStringURL() + "redirected")));
        stubFor(get(urlEqualTo("/redirected"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(FOLLOWED_REDIRECT)));

        return simpleGetApi.executeGet();
    }
}
