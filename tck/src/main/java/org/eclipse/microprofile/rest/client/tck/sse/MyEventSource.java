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

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jetty.servlets.EventSource;
import org.testng.log4testng.Logger;

public class MyEventSource implements EventSource {
    private static final Logger LOG = Logger.getLogger(AbstractSseTest.class);

    private Emitter emitter;
    private final Consumer<MyEventSource> consumer;
    private final CountDownLatch closeLatch = new CountDownLatch(1);

    MyEventSource(Consumer<MyEventSource> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void onOpen(Emitter emitter) throws IOException {
        this.emitter = emitter;
        ForkJoinPool.commonPool().submit(() -> {
            consumer.accept(this);
        });
    }

    @Override
    public void onClose() {
        emitter.close();
        emitter = null;
        closeLatch.countDown();
    }

    public void emitData(String data) {
        try {
            emitter.data(data);
            LOG.debug("emitted data: " + data);
        } catch (IOException e) {
            LOG.debug("Caught IOException", e);
            throw new RuntimeException(e);
        }
    }

    public void emitComment(String comment) {
        try {
            emitter.comment(comment);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void emitNamedEvent(String name, String data) {
        try {
            emitter.event(name, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        emitter.close();
    }

    public boolean awaitClose(long timeout, TimeUnit unit) {
        try {
            return closeLatch.await(timeout, unit);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
