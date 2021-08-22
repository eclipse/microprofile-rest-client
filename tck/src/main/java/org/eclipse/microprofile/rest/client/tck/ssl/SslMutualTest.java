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
import org.eclipse.microprofile.rest.client.tck.interfaces.ssl.ClientWithKeystoreAndTruststore;
import org.eclipse.microprofile.rest.client.tck.interfaces.ssl.ClientWithKeystoreFromClasspathAndTruststore;
import org.eclipse.microprofile.rest.client.tck.interfaces.ssl.ClientWithNonMatchingStore;
import org.eclipse.microprofile.rest.client.tck.interfaces.ssl.ClientWithTruststore;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;

public class SslMutualTest extends AbstractSslTest {

    @Inject
    @RestClient
    private JsonPClient clientWithNoSslStores;

    @Inject
    @RestClient
    private ClientWithTruststore clientWithTruststore;

    @Inject
    @RestClient
    private ClientWithKeystoreAndTruststore clientWithMutualSsl;

    @Inject
    @RestClient
    private ClientWithNonMatchingStore clientWithNonMatchingKeyStore;

    @Inject
    @RestClient
    private ClientWithKeystoreFromClasspathAndTruststore clientWithKeystoreFromClasspathAndTruststore;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class, SslMutualTest.class.getSimpleName() + ".war");

        initializeTest(webArchive,
                server -> server.keyStore(serverKeystore.getAbsolutePath(), PASSWORD)
                        .trustStore(serverTruststore.getAbsolutePath(), PASSWORD));

        // @formatter:off
        String config =
                configLine(JsonPClient.class, "uri", BASE_URI_STRING) +
                        configLine(ClientWithTruststore.class, "trustStore", filePath(clientTruststore)) +
                        configLine(ClientWithTruststore.class, "trustStoreType", "pkcs12") +
                        configLine(ClientWithTruststore.class, "trustStorePassword", PASSWORD) +
                        configLine(ClientWithTruststore.class, "uri", BASE_URI_STRING) +

                        configLine(ClientWithKeystoreAndTruststore.class, "trustStore", filePath(clientTruststore)) +
                        configLine(ClientWithKeystoreAndTruststore.class, "trustStoreType", "pkcs12") +
                        configLine(ClientWithKeystoreAndTruststore.class, "trustStorePassword", PASSWORD) +
                        configLine(ClientWithKeystoreAndTruststore.class, "keyStore", filePath(clientKeystore)) +
                        configLine(ClientWithKeystoreAndTruststore.class, "keyStoreType", "pkcs12") +
                        configLine(ClientWithKeystoreAndTruststore.class, "keyStorePassword", PASSWORD) +
                        configLine(ClientWithKeystoreAndTruststore.class, "uri", BASE_URI_STRING) +

                        configLine(ClientWithKeystoreFromClasspathAndTruststore.class, "trustStore",
                                filePath(clientTruststore))
                        +
                        configLine(ClientWithKeystoreFromClasspathAndTruststore.class, "trustStorePassword", PASSWORD) +
                        configLine(ClientWithKeystoreFromClasspathAndTruststore.class, "trustStoreType", "pkcs12") +
                        configLine(ClientWithKeystoreFromClasspathAndTruststore.class, "keyStore",
                                "classpath:/META-INF/" + clientKeystoreFromClasspath)
                        +
                        configLine(ClientWithKeystoreFromClasspathAndTruststore.class, "keyStoreType", "pkcs12") +
                        configLine(ClientWithKeystoreFromClasspathAndTruststore.class, "keyStorePassword", PASSWORD) +
                        configLine(ClientWithKeystoreFromClasspathAndTruststore.class, "uri", BASE_URI_STRING) +

                        configLine(ClientWithNonMatchingStore.class, "trustStore", filePath(clientTruststore)) +
                        configLine(ClientWithNonMatchingStore.class, "trustStoreType", "pkcs12") +
                        configLine(ClientWithNonMatchingStore.class, "trustStorePassword", PASSWORD) +
                        configLine(ClientWithNonMatchingStore.class, "keyStore", filePath(serverKeystore)) +
                        configLine(ClientWithNonMatchingStore.class, "keyStoreType", "pkcs12") +
                        configLine(ClientWithNonMatchingStore.class, "keyStorePassword", PASSWORD) +
                        configLine(ClientWithNonMatchingStore.class, "uri", BASE_URI_STRING);
        // @formatter:on
        webArchive
                .addClasses(JsonPClient.class, ClientWithTruststore.class, ClientWithNonMatchingStore.class,
                        ClientWithKeystoreAndTruststore.class, ClientWithKeystoreFromClasspathAndTruststore.class,
                        HttpsServer.class, AbstractSslTest.class)
                .addAsWebInfResource(new StringAsset(config), "classes/META-INF/microprofile-config.properties")
                .addAsWebInfResource(new ClassLoaderAsset("ssl/" + clientKeystoreFromClasspath),
                        "classes/META-INF/" + clientKeystoreFromClasspath)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return webArchive;
    }

    @Test(expectedExceptions = ProcessingException.class)
    public void shouldFailWithNoClientSignature() throws Exception {
        KeyStore trustStore = getKeyStore(clientTruststore);
        RestClientBuilder.newBuilder()
                .baseUri(BASE_URI)
                .trustStore(trustStore)
                .build(JsonPClient.class)
                .get("1");
    }

    @Test
    public void shouldWorkWithClientSignature() throws Exception {
        KeyStore trustStore = getKeyStore(clientTruststore);
        KeyStore keyStore = getKeyStore(clientKeystore);
        JsonPClient client = RestClientBuilder.newBuilder()
                .baseUri(BASE_URI)
                .trustStore(trustStore)
                .keyStore(keyStore, PASSWORD)
                .build(JsonPClient.class);
        assertEquals("bar", client.get("1").getString("foo"));
    }

    @Test(expectedExceptions = ProcessingException.class)
    public void shouldFailWithInvalidClientSignature() throws Exception {
        KeyStore trustStore = getKeyStore(clientTruststore);
        KeyStore wrongKeyStore = getKeyStore(serverKeystore);
        RestClientBuilder.newBuilder()
                .baseUri(BASE_URI)
                .trustStore(trustStore)
                .keyStore(wrongKeyStore, PASSWORD)
                .build(JsonPClient.class)
                .get("1");
    }

    @Test(expectedExceptions = ProcessingException.class)
    public void shouldFailWithNoClientSignatureCDI() {
        clientWithNoSslStores.get("1");
    }

    @Test
    public void shouldWorkWithClientSignatureCDI() {
        assertEquals("bar", clientWithMutualSsl.get("1").getString("foo"));
    }

    @Test
    public void shouldWorkWithClientSignatureFromClasspathCDI() {
        assertEquals("bar", clientWithKeystoreFromClasspathAndTruststore.get("1").getString("foo"));
    }

    @Test(expectedExceptions = ProcessingException.class)
    public void shouldFailWithInvalidClientSignatureCDI() {
        clientWithNonMatchingKeyStore.get("1");
    }
}
