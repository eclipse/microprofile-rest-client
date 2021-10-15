/*
 * Copyright (c) 2019, 2021 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.rest.client.tck.ssl;

import static org.eclipse.microprofile.rest.client.tck.utils.ConfigUtil.configLine;
import static org.testng.Assert.assertEquals;

import java.security.KeyStore;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.JsonPClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.ssl.ClientWithNonMatchingStore;
import org.eclipse.microprofile.rest.client.tck.interfaces.ssl.ClientWithTruststore;
import org.eclipse.microprofile.rest.client.tck.interfaces.ssl.JsonPClientWithTruststoreFromClasspath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;

public class SslTrustStoreTest extends AbstractSslTest {

    @Inject
    @RestClient
    private JsonPClient clientWithNoSslStores;

    @Inject
    @RestClient
    private ClientWithTruststore clientWithTruststore;

    @Inject
    @RestClient
    private ClientWithNonMatchingStore clientWithNonMatchingTruststore;

    @Inject
    @RestClient
    private JsonPClientWithTruststoreFromClasspath clientWithTruststoreFromClasspath;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class, SslTrustStoreTest.class.getSimpleName() + ".war");
        initializeTest(webArchive, server -> server.keyStore(serverKeystore.getAbsolutePath(), PASSWORD));

        // @formatter:off
        String config =
                configLine(JsonPClient.class, "uri", BASE_URI_STRING) +
                        configLine(ClientWithTruststore.class, "trustStore", filePath(clientTruststore)) +
                        configLine(ClientWithTruststore.class, "trustStoreType", "pkcs12") +
                        configLine(ClientWithTruststore.class, "trustStorePassword", PASSWORD) +
                        configLine(ClientWithTruststore.class, "uri", BASE_URI_STRING) +
                        configLine(JsonPClientWithTruststoreFromClasspath.class, "trustStore",
                                "classpath:/META-INF/" + clientTruststoreFromClasspath)
                        +
                        configLine(JsonPClientWithTruststoreFromClasspath.class, "trustStoreType", "pkcs12") +
                        configLine(JsonPClientWithTruststoreFromClasspath.class, "trustStorePassword", PASSWORD) +
                        configLine(JsonPClientWithTruststoreFromClasspath.class, "uri", BASE_URI_STRING) +
                        configLine(ClientWithNonMatchingStore.class, "trustStore", filePath(anotherTruststore)) +
                        configLine(ClientWithNonMatchingStore.class, "trustStoreType", "pkcs12") +
                        configLine(ClientWithNonMatchingStore.class, "trustStorePassword", PASSWORD) +
                        configLine(ClientWithNonMatchingStore.class, "uri", BASE_URI_STRING);
        // @formatter:on
        webArchive.addClasses(
                JsonPClient.class,
                ClientWithTruststore.class,
                ClientWithNonMatchingStore.class,
                JsonPClientWithTruststoreFromClasspath.class,
                HttpsServer.class,
                AbstractSslTest.class)
                .addAsWebInfResource(new StringAsset(config), "classes/META-INF/microprofile-config.properties")
                .addAsWebInfResource(new ClassLoaderAsset("ssl/" + clientTruststoreFromClasspath),
                        "classes/META-INF/" + clientTruststoreFromClasspath)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return webArchive;
    }

    @Test(expectedExceptions = ProcessingException.class)
    public void shouldFailWithSelfSignedKeystore() {
        RestClientBuilder.newBuilder()
                .baseUri(BASE_URI)
                .build(JsonPClient.class)
                .get("1");
    }

    @Test
    public void shouldSucceedWithRegisteredSelfSignedKeystore() throws Exception {
        KeyStore trustStore = getKeyStore(clientTruststore);
        JsonPClient client = RestClientBuilder.newBuilder()
                .baseUri(BASE_URI)
                .trustStore(trustStore)
                .build(JsonPClient.class);

        assertEquals("bar", client.get("1").getString("foo"));
    }

    @Test(expectedExceptions = ProcessingException.class)
    public void shouldFailWithNonMatchingKeystore() throws Exception {
        KeyStore ks = getKeyStore(anotherTruststore);

        RestClientBuilder.newBuilder()
                .baseUri(BASE_URI)
                .trustStore(ks)
                .build(JsonPClient.class)
                .get("1");
    }

    @Test(expectedExceptions = ProcessingException.class)
    public void shouldFailWithSelfSignedKeystoreCDI() {
        clientWithNoSslStores.get("1");
    }

    @Test
    public void shouldSucceedWithRegisteredSelfSignedKeystoreCDI() {
        assertEquals("bar", clientWithTruststore.get("1").getString("foo"));
    }

    @Test
    public void shouldSucceedWithRegisteredSelfSignedKeystoreFromResourceCDI() {
        assertEquals("bar", clientWithTruststoreFromClasspath.get("1").getString("foo"));
    }

    @Test(expectedExceptions = ProcessingException.class)
    public void shouldFailWithNonMatchingKeystoreCDI() {
        clientWithNonMatchingTruststore.get("1");
    }
}
