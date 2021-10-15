/*
 * Copyright (c) 2020, 2021 Contributors to the Eclipse Foundation
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

import jakarta.ws.rs.sse.InboundSseEvent;

public class BasicReactiveStreamsTest extends AbstractSseTest {

    private static final Logger LOG = Logger.getLogger(BasicReactiveStreamsTest.class);

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive webArchive = ShrinkWrap.create(WebArchive.class,
                BasicReactiveStreamsTest.class.getSimpleName() + ".war")
                .addClasses(AbstractSseTest.class,
                        BasicReactiveStreamsTest.class,
                        HttpSseServer.class,
                        MyEventSource.class,
                        MyEventSourceServlet.class,
                        RsSseClient.class,
                        RsWeatherEventClient.class,
                        WeatherEvent.class,
                        WeatherEventProvider.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

        return webArchive;
    }

    @Test
    public void testDataOnlySse_InboundSseEvent() throws Exception {
        CountDownLatch resultsLatch = new CountDownLatch(3);
        AtomicReference<Throwable> subscriptionException = new AtomicReference<>();
        AtomicReference<Throwable> serverException = launchServer(resultsLatch, es -> {
            es.emitData("foo");
            es.emitData("bar");
            es.emitData("baz");
        });

        RsSseClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:" + PORT + "/string/sse"))
                .build(RsSseClient.class);
        Publisher<InboundSseEvent> publisher = client.getEvents();
        InboundSseEventSubscriber subscriber = new InboundSseEventSubscriber(3, resultsLatch);
        publisher.subscribe(subscriber);

        assertTrue(resultsLatch.await(30, TimeUnit.SECONDS));
        assertEquals(subscriber.data, new HashSet<>(Arrays.asList("foo", "bar", "baz")));
        assertNull(serverException.get());
        assertNull(subscriptionException.get());
    }

    @Test
    public void testDataOnlySse_String() throws Exception {
        LOG.debug("testDataOnlySse_String");
        CountDownLatch resultsLatch = new CountDownLatch(3);
        AtomicReference<Throwable> serverException = launchServer(resultsLatch, es -> {
            es.emitData("foo2");
            es.emitData("bar2");
            es.emitData("baz2");
        });

        RsSseClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:" + PORT + "/string/sse"))
                .build(RsSseClient.class);
        Publisher<String> publisher = client.getStrings();
        StringSubscriber subscriber = new StringSubscriber(3, resultsLatch);
        publisher.subscribe(subscriber);
        assertTrue(resultsLatch.await(30, TimeUnit.SECONDS));
        assertEquals(subscriber.eventStrings, new HashSet<>(Arrays.asList("foo2", "bar2", "baz2")));
        assertNull(serverException.get());
        assertNull(subscriber.throwable);
    }

    @Test
    public void testDataOnlySse_JsonObject() throws Exception {
        LOG.debug("testDataOnlySse_JsonObject");
        CountDownLatch resultsLatch = new CountDownLatch(3);
        AtomicReference<Throwable> serverException = launchServer(resultsLatch, es -> {
            es.emitData("{\"date\":\"2020-01-21\", \"description\":\"Significant snowfall\"}");
            es.emitData("{\"date\":\"2020-02-16\", \"description\":\"Hail storm\"}");
            es.emitData("{\"date\":\"2020-04-12\", \"description\":\"Blizzard\"}");
        });

        RsWeatherEventClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:" + PORT + "/string/sse"))
                .build(RsWeatherEventClient.class);
        Publisher<WeatherEvent> publisher = client.getEvents();
        WeatherEventSubscriber subscriber = new WeatherEventSubscriber(3, resultsLatch);
        publisher.subscribe(subscriber);
        assertTrue(resultsLatch.await(30, TimeUnit.SECONDS));
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        assertEquals(subscriber.weatherEvents, new HashSet<>(Arrays.asList(
                new WeatherEvent(df.parse("2020-01-21"), "Significant snowfall"),
                new WeatherEvent(df.parse("2020-02-16"), "Hail storm"),
                new WeatherEvent(df.parse("2020-04-12"), "Blizzard"))));
        assertNull(serverException.get());
        assertNull(subscriber.throwable);
    }

    @Test
    public void testCommentOnlySse() throws Exception {
        CountDownLatch resultsLatch = new CountDownLatch(3);
        AtomicReference<Throwable> subscriptionException = new AtomicReference<>();
        AtomicReference<Throwable> serverException = launchServer(resultsLatch, es -> {
            es.emitComment("huey");
            es.emitComment("dewey");
            es.emitComment("louie");
        });

        RsSseClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:" + PORT + "/string/sse"))
                .build(RsSseClient.class);
        Publisher<InboundSseEvent> publisher = client.getEvents();
        InboundSseEventSubscriber subscriber = new InboundSseEventSubscriber(3, resultsLatch);
        publisher.subscribe(subscriber);

        assertTrue(resultsLatch.await(30, TimeUnit.SECONDS));
        assertEquals(subscriber.comments, new HashSet<>(Arrays.asList("huey", "dewey", "louie")));
        assertNull(serverException.get());
        assertNull(subscriptionException.get());
    }

    @Test
    public void testNamedEventSse() throws Exception {
        CountDownLatch resultsLatch = new CountDownLatch(3);
        AtomicReference<Throwable> subscriptionException = new AtomicReference<>();
        AtomicReference<Throwable> serverException = launchServer(resultsLatch, es -> {
            es.emitNamedEvent("1", "{\"date\":\"2020-01-21\", \"description\":\"Significant snowfall\"}");
            sleep(500);
            es.emitNamedEvent("2", "{\"date\":\"2020-02-16\", \"description\":\"Hail storm\"}");
            sleep(500);
            es.emitNamedEvent("3", "{\"date\":\"2020-04-12\", \"description\":\"Blizzard\"}");
        });

        RsSseClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:" + PORT + "/string/sse"))
                .build(RsSseClient.class);
        Publisher<InboundSseEvent> publisher = client.getEvents();
        InboundSseEventSubscriber subscriber = new InboundSseEventSubscriber(3, resultsLatch);
        publisher.subscribe(subscriber);

        assertTrue(resultsLatch.await(40, TimeUnit.SECONDS));
        assertEquals(subscriber.names, new HashSet<>(Arrays.asList("1", "2", "3")));
        assertEquals(subscriber.data, new HashSet<>(Arrays.asList(
                "{\"date\":\"2020-01-21\", \"description\":\"Significant snowfall\"}",
                "{\"date\":\"2020-02-16\", \"description\":\"Hail storm\"}",
                "{\"date\":\"2020-04-12\", \"description\":\"Blizzard\"}")));
        assertNull(serverException.get());
        assertNull(subscriptionException.get());
    }

    @Test
    public void testServerClosesConnection() throws Exception {
        CountDownLatch resultsLatch = new CountDownLatch(6);
        AtomicReference<Throwable> subscriptionException = new AtomicReference<>();
        AtomicReference<Throwable> serverException = launchServer(resultsLatch, es -> {
            es.emitData("one");
            es.emitData("two");
            sleep(500);
            es.emitData("three");
            sleep(500);
            es.emitData("four");
            es.emitData("five");
            sleep(500);
            es.close();
        });

        RsSseClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:" + PORT + "/string/sse"))
                .build(RsSseClient.class);
        Publisher<InboundSseEvent> publisher = client.getEvents();
        InboundSseEventSubscriber subscriber = new InboundSseEventSubscriber(20, resultsLatch) {
            @Override
            public void onComplete() {
                super.onComplete();
                eventLatch.countDown();
            }
        };
        publisher.subscribe(subscriber);

        assertTrue(resultsLatch.await(45, TimeUnit.SECONDS));
        assertEquals(subscriber.data, new HashSet<>(Arrays.asList("one", "two", "three", "four", "five")));
        assertTrue(subscriber.completed);
        assertNull(serverException.get());
        assertNull(subscriptionException.get());
    }

    /*
     * @Test public void testClientClosesConnection() throws Exception { AtomicBoolean clientClosedConnection = new
     * AtomicBoolean(false); CountDownLatch latch = new CountDownLatch(1); try (HttpSseServer server = new
     * HttpSseServer()) { server.start(PORT, es -> { es.emitNamedEvent("1", "Happy"); sleep(1000);
     * es.emitNamedEvent("2", "Meh"); sleep(1000); es.emitNamedEvent("3", "Sad");
     * clientClosedConnection.set(es.awaitClose(30, TimeUnit.SECONDS)); }); latch.await(30, TimeUnit.SECONDS);
     * assertTrue(clientClosedConnection.get(), "Client did not close connection"); } }
     */

    private static class InboundSseEventSubscriber implements Subscriber<InboundSseEvent>, AutoCloseable {

        final Set<String> data = new HashSet<>();
        final Set<String> comments = new HashSet<>();
        final Set<String> names = new HashSet<>();
        final Set<String> ids = new HashSet<>();
        final CountDownLatch eventLatch;
        Throwable throwable;
        boolean completed;
        Subscription subscription;
        long requestedEvents;

        InboundSseEventSubscriber(long requestedEvents, CountDownLatch eventLatch) {
            this.requestedEvents = requestedEvents;
            this.eventLatch = eventLatch;
        }

        @Override
        public void onSubscribe(Subscription s) {
            LOG.debug("InboundSseEventSubscriber onSubscribe " + s);
            subscription = s;
            s.request(requestedEvents);
        }

        @Override
        public void onNext(InboundSseEvent event) {
            LOG.debug("InboundSseEventSubscriber onNext " + event);
            data.add(event.readData());
            comments.add(event.getComment());
            names.add(event.getName());
            ids.add(event.getId());
            eventLatch.countDown();
        }

        @Override
        public void onError(Throwable t) {
            LOG.debug("InboundSseEventSubscriber onError " + t);
            throwable = t;
        }

        @Override
        public void onComplete() {
            LOG.debug("InboundSseEventSubscriber onComplete");
            completed = true;
        }

        @Override
        public void close() throws Exception {
            LOG.debug("InboundSseEventSubscriber close");
            subscription.cancel();
        }
    }

    private static class StringSubscriber implements Subscriber<String>, AutoCloseable {

        final Set<String> eventStrings = new HashSet<>();
        final CountDownLatch eventLatch;
        Throwable throwable;
        Subscription subscription;
        long requestedEvents;

        StringSubscriber(long requestedEvents, CountDownLatch eventLatch) {
            this.requestedEvents = requestedEvents;
            this.eventLatch = eventLatch;
        }

        @Override
        public void onSubscribe(Subscription s) {
            LOG.debug("StringSubscriber onSubscribe " + s);
            subscription = s;
            s.request(requestedEvents);
        }

        @Override
        public void onNext(String s) {
            LOG.debug("StringSubscriber onNext " + s);
            eventStrings.add(s);
            eventLatch.countDown();
        }

        @Override
        public void onError(Throwable t) {
            LOG.debug("StringSubscriber onError " + t);
            throwable = t;
        }

        @Override
        public void onComplete() {
            LOG.debug("StringSubscriber onComplete");
        }

        @Override
        public void close() throws Exception {
            LOG.debug("StringSubscriber close");
            subscription.cancel();
        }
    }

    private static class WeatherEventSubscriber implements Subscriber<WeatherEvent>, AutoCloseable {

        final Set<WeatherEvent> weatherEvents = new HashSet<>();
        final CountDownLatch eventLatch;
        Throwable throwable;
        Subscription subscription;
        long requestedEvents;

        WeatherEventSubscriber(long requestedEvents, CountDownLatch eventLatch) {
            this.requestedEvents = requestedEvents;
            this.eventLatch = eventLatch;
        }

        @Override
        public void onSubscribe(Subscription s) {
            LOG.debug("WeatherEventSubscriber onSubscribe " + s);
            subscription = s;
            s.request(requestedEvents);
        }

        @Override
        public void onNext(WeatherEvent s) {
            LOG.debug("WeatherEventSubscriber onNext " + s);
            weatherEvents.add(s);
            eventLatch.countDown();
        }

        @Override
        public void onError(Throwable t) {
            LOG.debug("WeatherEventSubscriber onError " + t);
            throwable = t;
        }

        @Override
        public void onComplete() {
            LOG.debug("WeatherEventSubscriber onComplete");
        }

        @Override
        public void close() throws Exception {
            LOG.debug("WeatherEventSubscriber close");
            subscription.cancel();
        }
    }
}
