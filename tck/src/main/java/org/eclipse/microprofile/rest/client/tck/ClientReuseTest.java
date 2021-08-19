/*
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
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
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.JsonPClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ClientReuseTest extends WiremockArquillianTest {

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, ClientReuseTest.class.getSimpleName() + ".war")
                .addClasses(WiremockArquillianTest.class, JsonPClient.class);
    }

    @Test
    public void shouldReuseClientAfterFailure() throws Throwable {
        stubReturning(1, "{\"content\": true}");
        stubReturning(2, "Not a json");
        stubReturning(3, "{\"content\": true}");

        callWithTimeout(this::testReuseClientAfterFailure, 20, TimeUnit.SECONDS);
    }

    private Void testReuseClientAfterFailure() {
        JsonPClient client =
                RestClientBuilder.newBuilder()
                        .baseUri(getServerURI())
                        .build(JsonPClient.class);

        // expect a successful invocation
        assertTrue(client.get("1").getBoolean("content"));

        // expect a failing invocation
        expectFailure(() -> client.get("2"));

        // expect a successful invocation
        assertTrue(client.get("3").getBoolean("content"));

        return null;
    }

    private void stubReturning(int id, String text) {
        stubFor(
                get(urlEqualTo("/" + id))
                        .willReturn(
                                aResponse().withBody(text).withHeader("Content-Type", "application/json")));
    }

    private void callWithTimeout(Callable<Object> timedCallable, long timeout, TimeUnit timeUnit) throws Throwable {
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        try {
            List<Future<Object>> futures =
                    executorService.invokeAll(Collections.singleton(timedCallable), timeout, timeUnit);
            futures.iterator().next().get();
        } catch (InterruptedException | CancellationException e) {
            e.printStackTrace();
            Assert.fail("the test didn't finish in " + timeout + " " + timeUnit);
        } catch (ExecutionException e) {
            Throwable unwrappedError = e.getCause();
            throw unwrappedError;
        }
    }

    private void expectFailure(Callable<?> callable) {
        try {
            // this request should fail
            callable.call();
        } catch (Exception ignored) {
            return;
        }
        fail("The call that was expected to fall succeeded");
    }
}
