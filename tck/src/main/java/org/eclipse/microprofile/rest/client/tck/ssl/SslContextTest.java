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

import javax.net.ssl.SSLContext;

import org.apache.http.ssl.SSLContextBuilder;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.JsonPClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.ws.rs.ProcessingException;

public class SslContextTest extends AbstractSslTest {

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class, SslContextTest.class.getSimpleName() + ".war")
                .addClasses(JsonPClient.class, HttpsServer.class, AbstractSslTest.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        initializeTest(webArchive,
                server -> server.keyStore(serverKeystore.getAbsolutePath(), PASSWORD)
                        .trustStore(serverTruststore.getAbsolutePath(), PASSWORD));

        return webArchive;
    }

    @Test
    public void shouldSucceedMutualSslWithValidSslContext() throws Exception {
        SSLContext sslContext = SSLContextBuilder.create()
                .loadKeyMaterial(getKeyStore(clientKeystore), PASSWORD.toCharArray())
                .loadTrustMaterial(getKeyStore(clientTruststore), null)
                .build();
        RestClientBuilder.newBuilder()
                .baseUri(BASE_URI)
                .sslContext(sslContext)
                .build(JsonPClient.class)
                .get("1");
    }

    @Test(expectedExceptions = ProcessingException.class)
    public void shouldFailedMutualSslWithoutSslContext() {
        RestClientBuilder.newBuilder()
                .baseUri(BASE_URI)
                .build(JsonPClient.class)
                .get("1");
    }
}
