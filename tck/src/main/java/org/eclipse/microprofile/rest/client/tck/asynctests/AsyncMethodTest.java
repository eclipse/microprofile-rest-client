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

package org.eclipse.microprofile.rest.client.tck.asynctests;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.WiremockArquillianTest;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApiAsync;
import org.eclipse.microprofile.rest.client.tck.interfaces.StringResponseClientAsync;
import org.eclipse.microprofile.rest.client.tck.providers.TLAddPathClientRequestFilter;
import org.eclipse.microprofile.rest.client.tck.providers.TLAsyncInvocationInterceptor;
import org.eclipse.microprofile.rest.client.tck.providers.TLAsyncInvocationInterceptorFactory;
import org.eclipse.microprofile.rest.client.tck.providers.TLClientResponseFilter;
import org.eclipse.microprofile.rest.client.tck.providers.ThreadedClientResponseFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.Response;

/**
 * Verifies via CDI injection that you can use a programmatic interface. verifies that the interface has Dependent
 * scope. This test is the same as the
 * {@link org.eclipse.microprofile.rest.client.tck.cditests.CDIInvokeSimpleGetOperationTest} but uses async methods.
 */
public class AsyncMethodTest extends WiremockArquillianTest {

    @Deployment
    public static WebArchive createDeployment() {
        String simpleName = AsyncMethodTest.class.getSimpleName();
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
                .addClasses(WiremockArquillianTest.class,
                        SimpleGetApiAsync.class,
                        StringResponseClientAsync.class,
                        ThreadedClientResponseFilter.class,
                        TLAsyncInvocationInterceptorFactory.class,
                        TLAsyncInvocationInterceptor.class,
                        TLAddPathClientRequestFilter.class,
                        TLClientResponseFilter.class);
    }

    /**
     * Tests that a Rest Client interface method that returns CompletionStage is invoked asychronously - checking that
     * the thread ID of the response does not match the thread ID of the calling thread.
     *
     * @throws Exception
     *             - indicates test failure
     */
    // Commented out for now. May revisit in future an alternative way to test asynchronous handling.
    // @Test
    public void testInterfaceMethodWithCompletionStageResponseReturnIsInvokedAsynchronously() throws Exception {
        final String expectedBody = "Hello, Async Client!";
        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withBody(expectedBody)));

        final String mainThreadId = "" + Thread.currentThread().getId();

        SimpleGetApiAsync api = RestClientBuilder.newBuilder()
                .baseUrl(getServerURL())
                .register(ThreadedClientResponseFilter.class)
                .build(SimpleGetApiAsync.class);
        CompletionStage<Response> future = api.executeGet();

        Response response = future.toCompletableFuture().get();
        String body = response.readEntity(String.class);

        response.close();

        String responseThreadId = response.getHeaderString(ThreadedClientResponseFilter.RESPONSE_THREAD_ID_HEADER);
        assertNotNull(responseThreadId);
        assertNotEquals(responseThreadId, mainThreadId);
        assertEquals(body, expectedBody);

        verify(1, getRequestedFor(urlEqualTo("/")));
    }

    /**
     * Tests that a Rest Client interface method that returns a CompletionStage where it's parameterized type is some
     * Object type other than Response) is invoked asychronously - checking that the thread ID of the response does not
     * match the thread ID of the calling thread.
     *
     * @throws Exception
     *             - indicates test failure
     */
    @Test
    public void testInterfaceMethodWithCompletionStageObjectReturnIsInvokedAsynchronously() throws Exception {
        final String expectedBody = "Hello, Future Async Client!!";
        stubFor(get(urlEqualTo("/string"))
                .willReturn(aResponse()
                        .withBody(expectedBody)));

        final String mainThreadId = "" + Thread.currentThread().getId();

        ThreadedClientResponseFilter filter = new ThreadedClientResponseFilter();
        StringResponseClientAsync client = RestClientBuilder.newBuilder()
                .baseUrl(getServerURL())
                .register(filter)
                .build(StringResponseClientAsync.class);
        CompletionStage<String> future = client.get();

        String body = future.toCompletableFuture().get();

        String responseThreadId = filter.getResponseThreadId();
        assertNotNull(responseThreadId);
        assertNotEquals(responseThreadId, mainThreadId);
        assertEquals(body, expectedBody);

        verify(1, getRequestedFor(urlEqualTo("/string")));
    }

    /**
     * Tests that the MP Rest Client implementation uses the specified ExecutorService.
     *
     * @throws Exception
     *             - indicates test failure
     */
    @Test
    public void testExecutorService() throws Exception {
        final String expectedBody = "Hello, InvocationCallback Async Client!!!";
        final String expectedThreadName = "MPRestClientTCKThread";
        stubFor(get(urlEqualTo("/execSvc"))
                .willReturn(aResponse()
                        .withBody(expectedBody)));

        final long mainThreadId = Thread.currentThread().getId();

        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, expectedThreadName);
            }
        };
        ExecutorService testExecutorService = Executors.newSingleThreadExecutor(threadFactory);

        SimpleGetApiAsync client = RestClientBuilder.newBuilder()
                .baseUrl(getServerURL())
                .register(ThreadedClientResponseFilter.class)
                .executorService(testExecutorService)
                .build(SimpleGetApiAsync.class);

        CompletionStage<Response> future = client.executeGetExecSvc();

        Response r = future.toCompletableFuture().get();

        assertEquals(r.readEntity(String.class), expectedBody);
        assertNotEquals(
                r.getHeaderString(ThreadedClientResponseFilter.RESPONSE_THREAD_ID_HEADER),
                mainThreadId);
        assertEquals(
                r.getHeaderString(ThreadedClientResponseFilter.RESPONSE_THREAD_NAME_HEADER),
                expectedThreadName);

        verify(1, getRequestedFor(urlEqualTo("/execSvc")));
    }

    /**
     * This test uses a <code>ClientRequestFilter</code> to update the destination URI. It attempts to update it based
     * on a ThreadLocal object on the calling thread. It uses an <code>AsyncInvocationInterceptorFactory</code> provider
     * to copy the ThreadLocal value from the calling thread to the async thread.
     *
     * @throws Exception
     *             - indicates test failure
     */
    @Test
    public void testAsyncInvocationInterceptorProvider() throws Exception {
        final String expectedBody = "Hello, Async Intercepted Client!!";
        final Integer threadLocalInt = 808;
        final long mainThreadId = Thread.currentThread().getId();

        stubFor(get(urlEqualTo("/" + threadLocalInt))
                .willReturn(aResponse()
                        .withBody(expectedBody)));

        AtomicBoolean isThreadLocalCleared = new AtomicBoolean(true);
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(new Runnable() {
                    @Override
                    public void run() {
                        isThreadLocalCleared.set(TLAsyncInvocationInterceptorFactory.getTlInt() == 0);
                        r.run();
                    }
                });
            }
        };
        ExecutorService testExecutorService = Executors.newSingleThreadExecutor(threadFactory);

        final TLAsyncInvocationInterceptorFactory aiiFactory = new TLAsyncInvocationInterceptorFactory(threadLocalInt);
        final TLClientResponseFilter responseFilter = new TLClientResponseFilter();
        SimpleGetApiAsync api = RestClientBuilder.newBuilder()
                .baseUrl(getServerURL())
                .register(TLAddPathClientRequestFilter.class)
                .register(aiiFactory)
                .register(responseFilter)
                .executorService(testExecutorService)
                .build(SimpleGetApiAsync.class);
        CompletionStage<Response> future = api.executeGet();

        Response response = future.toCompletableFuture().get();
        assertEquals(response.getStatus(), 200);
        String sentUri = response.getHeaderString("Sent-URI");
        assertTrue(sentUri.endsWith("/" + threadLocalInt));
        assertEquals((long) TLAsyncInvocationInterceptorFactory.getTlInt(), 808L);
        assertEquals((long) responseFilter.getThreadLocalIntDuringResponse(), (long) threadLocalInt);

        String body = response.readEntity(String.class);

        response.close();

        // force the test to wait for async thread to be recycled and re-used
        // and verify that the thread locals have been cleared.
        Future<?> f = testExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("Reusing single thread pool thread");
            }
        });
        f.get(30, TimeUnit.SECONDS);
        assertTrue(isThreadLocalCleared.get());
        assertEquals(body, expectedBody);
        Map<String, Object> data = aiiFactory.getData();
        assertEquals(data.get("preThreadId"), mainThreadId);
        assertNotEquals(data.get("postThreadId"), mainThreadId);
        assertEquals(data.get("removeThreadId"), data.get("postThreadId"));
        assertEquals(data.get("AsyncThreadLocalPre"), 808);
        assertEquals(data.get("AsyncThreadLocalPost"), 0);

        verify(1, getRequestedFor(urlEqualTo("/" + threadLocalInt)));
    }

    /**
     * This test verifies that the <code>RestClientBuilder</code> implementation will throw an
     * <code>IllegalArgumentException</code> when a null value is passed to the <code>executorService</code> method.
     *
     * @throws IllegalArgumentException
     *             - expected when passing null
     */
    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void testNullExecutorServiceThrowsIllegalArgumentException() {
        RestClientBuilder.newBuilder().executorService(null);
        fail("Passing a null ExecutorService should result in an IllegalArgumentException");
    }
}
