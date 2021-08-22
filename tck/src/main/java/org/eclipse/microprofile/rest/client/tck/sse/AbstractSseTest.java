/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.microprofile.rest.client.tck.sse;

import static org.testng.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.testng.log4testng.Logger;

public abstract class AbstractSseTest {

    private static final Logger LOG = Logger.getLogger(AbstractSseTest.class);
    protected static final int PORT = Integer.getInteger("sse.server.port", 10000);

    private static ExecutorService serverLaunchExecutor = Executors.newSingleThreadExecutor();

    protected static AtomicReference<Throwable> launchServer(CountDownLatch stopLatch,
            Consumer<MyEventSource> consumer) throws Exception {
        return launchServer(stopLatch, consumer, null);
    }

    protected static AtomicReference<Throwable> launchServer(CountDownLatch stopLatch,
            Consumer<MyEventSource> consumer, CountDownLatch cleanupLatch) throws Exception {

        AtomicReference<Throwable> caughtException = new AtomicReference<>();
        CountDownLatch startLatch = new CountDownLatch(1);
        serverLaunchExecutor.submit(() -> {
            try (HttpSseServer server = new HttpSseServer()) {
                server.start(PORT, consumer);
                startLatch.countDown();
                LOG.info("launchServer server started on port " + PORT);
                assertTrue(stopLatch.await(30, TimeUnit.SECONDS),
                        "Timed out with client expecting " + stopLatch.getCount() + " unreceived event(s)");
            } catch (Throwable t) {
                LOG.error("launchServer caughtException ", t);
                caughtException.set(t);
            } finally {
                if (cleanupLatch != null) {
                    cleanupLatch.countDown();
                }
            }
        });
        assertTrue(startLatch.await(30, TimeUnit.SECONDS), "Mock Sse Server did not start as expected");
        return caughtException;
    }

    protected static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public abstract void testDataOnlySse_InboundSseEvent() throws Exception;

    public abstract void testDataOnlySse_String() throws Exception;

    public abstract void testDataOnlySse_JsonObject() throws Exception;

    public abstract void testCommentOnlySse() throws Exception;

    public abstract void testNamedEventSse() throws Exception;

    public abstract void testServerClosesConnection() throws Exception;
}
