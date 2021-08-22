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
import static org.testng.Assert.assertNotNull;

import java.security.KeyStore;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.JsonPClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.ssl.ConfigurableHostnameVerifier;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.ws.rs.ProcessingException;

public class SslHostnameVerifierTest extends AbstractSslTest {

    @Deployment
    public static WebArchive createDeployment() {
        // @formatter:off
        String config =
                configLine(JsonPClient.class, "uri", BASE_URI_STRING) +
                        configLine(JsonPClient.class, "hostnameVerifier",
                                ConfigurableHostnameVerifier.class.getCanonicalName())
                        +
                        configLine(JsonPClient.class, "trustStore",
                                "classpath:/META-INF/" + clientWrongHostnameTruststoreFromClasspath)
                        +
                        configLine(JsonPClient.class, "trustStoreType", "pkcs12") +
                        configLine(JsonPClient.class, "trustStorePassword", PASSWORD);
        // @formatter:on

        WebArchive webArchive =
                ShrinkWrap.create(WebArchive.class, SslHostnameVerifierTest.class.getSimpleName() + ".war")
                        .addClasses(
                                JsonPClient.class,
                                HttpsServer.class,
                                AbstractSslTest.class,
                                ConfigurableHostnameVerifier.class)
                        .addAsWebInfResource(new StringAsset(config), "classes/META-INF/microprofile-config.properties")
                        .addAsWebInfResource(
                                new ClassLoaderAsset("ssl/" + clientWrongHostnameTruststoreFromClasspath),
                                "classes/META-INF/" + clientWrongHostnameTruststoreFromClasspath)
                        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        initializeTest(webArchive, server -> server.keyStore(serverWrongHostnameKeystore.getAbsolutePath(), PASSWORD));
        return webArchive;
    }

    @Inject
    @RestClient
    private JsonPClient clientWithHostnameVerifier;

    @Test(expectedExceptions = ProcessingException.class)
    public void shouldFailWithoutHostnameAndNoVerifier() throws Exception {
        KeyStore trustStore = getKeyStore(clientWrongHostnameTruststore);
        RestClientBuilder.newBuilder()
                .baseUri(BASE_URI)
                .trustStore(trustStore)
                .build(JsonPClient.class)
                .get("1");
    }

    @Test(expectedExceptions = ProcessingException.class)
    public void shouldFailWithRejectingHostnameVerifier() throws Exception {
        KeyStore trustStore = getKeyStore(clientWrongHostnameTruststore);
        RestClientBuilder.newBuilder()
                .baseUri(BASE_URI)
                .trustStore(trustStore)
                .hostnameVerifier((s, sslSession) -> false)
                .build(JsonPClient.class)
                .get("1");
    }

    @Test
    public void shouldSucceedWithAcceptingHostnameVerifier() throws Exception {
        KeyStore trustStore = getKeyStore(clientWrongHostnameTruststore);
        JsonPClient client = RestClientBuilder.newBuilder()
                .baseUri(BASE_URI)
                .trustStore(trustStore)
                .hostnameVerifier((s, sslSession) -> true)
                .build(JsonPClient.class);

        assertEquals("bar", client.get("1").getString("foo"));
    }

    @Test
    public void shouldPassSslSessionAndHostnameToHostnameVerifier() throws Exception {
        KeyStore trustStore = getKeyStore(clientWrongHostnameTruststore);
        JsonPClient client = RestClientBuilder.newBuilder()
                .baseUri(BASE_URI)
                .trustStore(trustStore)
                .hostnameVerifier(this::verifySslSessionAndHostname)
                .build(JsonPClient.class);

        assertEquals("bar", client.get("1").getString("foo"));
    }

    @Test(expectedExceptions = ProcessingException.class)
    public void shouldFailWithRejectingHostnameVerifierCDI() {
        ConfigurableHostnameVerifier.setAccepting(false);
        clientWithHostnameVerifier.get("1");
    }

    @Test
    public void shouldSucceedWithAcceptingHostnameVerifierCDI() {
        ConfigurableHostnameVerifier.setAccepting(true);

        assertEquals("bar", clientWithHostnameVerifier.get("1").getString("foo"));
    }

    @Test
    public void shouldPassSslSessionAndHostnameToHostnameVerifierCDI() {
        ConfigurableHostnameVerifier.setAccepting(true);

        assertEquals("bar", clientWithHostnameVerifier.get("1").getString("foo"));
        verifySslSessionAndHostname(ConfigurableHostnameVerifier.getHostname(),
                ConfigurableHostnameVerifier.getSslSession());
    }

    private boolean verifySslSessionAndHostname(String hostname, SSLSession sslSession) {
        try {
            assertEquals("localhost", hostname);
            assertNotNull(sslSession);
            assertNotNull(sslSession.getCipherSuite());
            assertNotNull(sslSession.getPeerCertificates());
            return true;
        } catch (SSLPeerUnverifiedException e) {
            throw new RuntimeException("failed to verify ssl session and hostname", e);
        }
    }

}
