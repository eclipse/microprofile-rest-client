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

import static org.eclipse.microprofile.rest.client.tck.sse.AbstractSseTest.launchServer;
import static org.eclipse.microprofile.rest.client.tck.sse.AbstractSseTest.PORT;

import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.sse.InboundSseEvent;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.reactivestreams.Publisher;
import org.reactivestreams.tck.PublisherVerification;
import org.reactivestreams.tck.TestEnvironment;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

@Test(singleThreaded = true)
public class ReactiveStreamsPublisherTckTest extends PublisherVerification<InboundSseEvent> {
    private static final Logger LOG = Logger.getLogger(ReactiveStreamsPublisherTckTest.class);

    private CountDownLatch cleanupLatch;
    private AtomicBoolean inMethod = new AtomicBoolean(false);

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive webArchive = ShrinkWrap
                .create(WebArchive.class, BasicReactiveStreamsTest.class.getSimpleName() + ".war")
                .addPackage(PublisherVerification.class.getPackage())
                .addClasses(ReactiveStreamsPublisherTckTest.class, HttpSseServer.class, MyEventSource.class,
                        MyEventSourceServlet.class, RsSseClient.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return webArchive;
    }

    public ReactiveStreamsPublisherTckTest() {
        super(new TestEnvironment(1000));
    }

    @BeforeMethod
    private void setupLatch() {
        cleanupLatch = new CountDownLatch(1);
        inMethod.compareAndSet(false, true);
    }

    @AfterMethod
    private void countDownLatch() throws InterruptedException {
        inMethod.compareAndSet(true, false);
        if (!cleanupLatch.await(30, TimeUnit.SECONDS)) {
            LOG.error("Server did not close long after test completed");
        }
    }

    @Override
    public Publisher<InboundSseEvent> createFailedPublisher() {
        cleanupLatch.countDown();
        return null; // TODO: implement for failed publisher test support (optional tests)
    }

    @Override
    public Publisher<InboundSseEvent> createPublisher(long elements) {
        LOG.debug("createPublisher (" + elements + ")");
        CountDownLatch latch = new CountDownLatch(1);
        try {
            AtomicReference<Throwable> serverException = launchServer(latch, es -> {
                for (long i = 0; i < elements; i++) {
                    if (inMethod.get()) {
                        es.emitData(Long.toString(i));
                    }
                }
                latch.countDown();
            }, cleanupLatch);

            if (serverException.get() != null) {
                throw serverException.get();
            }

            RsSseClient client = RestClientBuilder.newBuilder()
                    .baseUri(URI.create("http://localhost:" + PORT + "/string/sse")).build(RsSseClient.class);
            Publisher<InboundSseEvent> publisher = client.getEvents();
            LOG.debug("createPublisher --> " + publisher);
            return publisher;
        }
        catch (Throwable t) {
            LOG.error("Failed to create publisher", t);
            t.printStackTrace();
            return null;
        }
    }

}
