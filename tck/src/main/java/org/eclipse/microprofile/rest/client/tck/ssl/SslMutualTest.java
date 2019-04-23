/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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

import org.eclipse.microprofile.rest.client.RestClientBuilder;
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

import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import java.security.KeyStore;

import static org.junit.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

/**
 * @author Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * <br>
 * Date: 16/04/2019
 */
public class SslMutualTest extends AbstractSslTest {

    @Inject
    private JsonPClient clientWithNoSslStores;

    @Inject
    private ClientWithTruststore clientWithTruststore;

    @Inject
    private ClientWithKeystoreAndTruststore clientWithMutualSsl;

    @Inject
    private ClientWithNonMatchingStore clientWithNonMatchingKeyStore;

    @Inject
    private ClientWithKeystoreFromClasspathAndTruststore clientWithKeystoreFromClasspathAndTruststore;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class);

        initializeTest(webArchive,
            server ->
                server.keyStore(serverKeystore.getAbsolutePath(), PASSWORD)
                    .trustStore(serverTruststore.getAbsolutePath(), PASSWORD)
        );

        // @formatter:off
        String config =
            configLine(JsonPClient.class, "uri", BASE_URI_STRING) +
            configLine(ClientWithTruststore.class, "trustStore", clientTruststore.getAbsolutePath()) +
            configLine(ClientWithTruststore.class, "trustStorePassword", PASSWORD) +
            configLine(ClientWithTruststore.class, "uri", BASE_URI_STRING) +

            configLine(ClientWithKeystoreAndTruststore.class, "trustStore", clientTruststore.getAbsolutePath()) +
            configLine(ClientWithKeystoreAndTruststore.class, "trustStorePassword", PASSWORD) +
            configLine(ClientWithKeystoreAndTruststore.class, "keyStore", clientKeystore.getAbsolutePath()) +
            configLine(ClientWithKeystoreAndTruststore.class, "keyStorePassword", PASSWORD) +
            configLine(ClientWithKeystoreAndTruststore.class, "uri", BASE_URI_STRING) +

            configLine(ClientWithKeystoreFromClasspathAndTruststore.class, "trustStore", clientTruststore.getAbsolutePath()) +
            configLine(ClientWithKeystoreFromClasspathAndTruststore.class, "trustStorePassword", PASSWORD) +
            configLine(ClientWithKeystoreFromClasspathAndTruststore.class, "keyStore", "classpath:/META-INF/" + clientKeystoreFromClasspath) +
            configLine(ClientWithKeystoreFromClasspathAndTruststore.class, "keyStorePassword", PASSWORD) +
            configLine(ClientWithKeystoreFromClasspathAndTruststore.class, "uri", BASE_URI_STRING) +

            configLine(ClientWithNonMatchingStore.class, "trustStore", clientTruststore.getAbsolutePath()) +
            configLine(ClientWithNonMatchingStore.class, "trustStorePassword", PASSWORD) +
            configLine(ClientWithNonMatchingStore.class, "keyStore", serverKeystore.getAbsolutePath()) +
            configLine(ClientWithNonMatchingStore.class, "keyStorePassword", PASSWORD) +
            configLine(ClientWithNonMatchingStore.class, "uri", BASE_URI_STRING);
        // @formatter:on
        webArchive
            .addClasses(JsonPClient.class, ClientWithTruststore.class, ClientWithNonMatchingStore.class,
                ClientWithKeystoreAndTruststore.class, ClientWithKeystoreFromClasspathAndTruststore.class,
                HttpsServer.class, AbstractSslTest.class)
            .addAsManifestResource(new StringAsset(config), "microprofile-config.properties")
            .addAsManifestResource(new ClassLoaderAsset("ssl/" + clientKeystoreFromClasspath), clientKeystoreFromClasspath)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return webArchive;
    }

    @Test
    public void shouldFailWithNoClientSignature() throws Exception {
        KeyStore trustStore = getKeyStore(clientTruststore);
        assertThrows(ProcessingException.class, () ->
            RestClientBuilder.newBuilder()
                .baseUri(BASE_URI)
                .trustStore(trustStore)
                .build(JsonPClient.class)
                .get("1")
        );
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

    @Test
    public void shouldFailWithInvalidClientSignature() throws Exception {
        KeyStore trustStore = getKeyStore(clientTruststore);
        KeyStore wrongKeyStore = getKeyStore(serverKeystore);
        assertThrows(ProcessingException.class, () ->
            RestClientBuilder.newBuilder()
                .baseUri(BASE_URI)
                .trustStore(trustStore)
                .keyStore(wrongKeyStore, PASSWORD)
                .build(JsonPClient.class)
                .get("1")
        );
    }

    @Test
    public void shouldFailWithNoClientSignatureCDI() {
        assertThrows(ProcessingException.class, () ->
            clientWithNoSslStores.get("1")
        );
    }

    @Test
    public void shouldWorkWithClientSignatureCDI() {
        assertEquals("bar", clientWithMutualSsl.get("1").getString("foo"));
    }

    @Test
    public void shouldWorkWithClientSignatureFromClasspathCDI() {
        assertEquals("bar", clientWithKeystoreFromClasspathAndTruststore.get("1").getString("foo"));
    }

    @Test
    public void shouldFailWithInvalidClientSignatureCDI() {
        assertThrows(ProcessingException.class, () ->
            clientWithNonMatchingKeyStore.get("1")
        );
    }


}
