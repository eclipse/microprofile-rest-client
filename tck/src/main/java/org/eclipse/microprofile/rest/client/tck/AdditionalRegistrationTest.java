/*
 * Copyright 2017, 2021 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.tck;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.eclipse.microprofile.rest.client.tck.providers.InjectedSimpleFeature;
import org.eclipse.microprofile.rest.client.tck.providers.MultiTypedProvider;
import org.eclipse.microprofile.rest.client.tck.providers.TestClientRequestFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseFilter;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.ReaderInterceptor;
import jakarta.ws.rs.ext.WriterInterceptor;

public class AdditionalRegistrationTest extends Arquillian {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, AdditionalRegistrationTest.class.getSimpleName() + ".war")
                .addPackage(InjectedSimpleFeature.class.getPackage());
    }

    @Test
    public void shouldRegisterInstance() {
        TestClientRequestFilter instance = new TestClientRequestFilter();
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(instance);
        Configuration configuration = builder.getConfiguration();
        assertTrue(configuration.isRegistered(TestClientRequestFilter.class),
                TestClientRequestFilter.class + " should be registered");
        assertTrue(configuration.isRegistered(instance), TestClientRequestFilter.class + " should be registered");
    }

    @Test
    public void shouldRegisterInstanceWithPriority() {
        Integer priority = 1000;
        TestClientRequestFilter instance = new TestClientRequestFilter();
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(instance, priority);
        Configuration configuration = builder.getConfiguration();
        assertTrue(configuration.isRegistered(TestClientRequestFilter.class),
                TestClientRequestFilter.class + " should be registered");
        assertTrue(configuration.isRegistered(instance), TestClientRequestFilter.class + " should be registered");
        Map<Class<?>, Integer> contracts = configuration.getContracts(TestClientRequestFilter.class);
        assertEquals(contracts.size(), 1, "There should be a registered contract for " + TestClientRequestFilter.class);
        assertEquals(contracts.get(ClientRequestFilter.class), priority,
                "The priority for " + TestClientRequestFilter.class + " should be 1000");
    }

    @Test
    public void shouldRegisterAMultiTypedProviderInstance() {
        MultiTypedProvider provider = new MultiTypedProvider();
        Class<?>[] providerTypes = {ClientRequestFilter.class, ClientResponseFilter.class,
                MessageBodyReader.class, MessageBodyWriter.class, ReaderInterceptor.class, WriterInterceptor.class,
                ResponseExceptionMapper.class, ParamConverterProvider.class};
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(provider, providerTypes);
        Configuration configuration = builder.getConfiguration();
        assertTrue(configuration.isRegistered(MultiTypedProvider.class),
                MultiTypedProvider.class + " should be registered");
        assertTrue(configuration.isRegistered(provider), MultiTypedProvider.class + " should be registered");
        assertEquals(configuration.getContracts(MultiTypedProvider.class).size(), providerTypes.length,
                "There should be " + providerTypes.length + " provider types registered");
    }

    @Test
    public void shouldRegisterAMultiTypedProviderInstanceWithPriorities() {
        MultiTypedProvider provider = new MultiTypedProvider();
        Map<Class<?>, Integer> priorities = new HashMap<>();
        priorities.put(ClientRequestFilter.class, 500);
        priorities.put(ClientResponseFilter.class, 501);
        priorities.put(MessageBodyReader.class, 502);
        priorities.put(MessageBodyWriter.class, 503);
        priorities.put(ReaderInterceptor.class, 504);
        priorities.put(WriterInterceptor.class, 505);
        priorities.put(ResponseExceptionMapper.class, 506);
        priorities.put(ParamConverterProvider.class, 507);
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(provider, priorities);
        Configuration configuration = builder.getConfiguration();
        assertTrue(configuration.isRegistered(MultiTypedProvider.class),
                MultiTypedProvider.class + " should be registered");
        assertTrue(configuration.isRegistered(provider), MultiTypedProvider.class + " should be registered");
        Map<Class<?>, Integer> contracts = configuration.getContracts(MultiTypedProvider.class);
        assertEquals(contracts.size(), priorities.size(),
                "There should be " + priorities.size() + " provider types registered");
        for (Map.Entry<Class<?>, Integer> priority : priorities.entrySet()) {
            Integer contractPriority = contracts.get(priority.getKey());
            assertEquals(contractPriority, priority.getValue(),
                    "The priority for " + priority.getKey() + " should be " + priority.getValue());
        }
    }

    @Test
    public void shouldRegisterProvidersWithPriority() {
        Integer priority = 1000;
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(TestClientRequestFilter.class, priority);
        Configuration configuration = builder.getConfiguration();
        assertTrue(configuration.isRegistered(TestClientRequestFilter.class),
                TestClientRequestFilter.class + " should be registered");
        Map<Class<?>, Integer> contracts = configuration.getContracts(TestClientRequestFilter.class);
        assertEquals(contracts.size(), 1, "There should be a registered contract for " + TestClientRequestFilter.class);
        assertEquals(contracts.get(ClientRequestFilter.class), priority,
                "The priority for " + TestClientRequestFilter.class + " should be 1000");
    }

    @Test
    public void shouldRegisterAMultiTypedProviderClass() {
        Class<?>[] providerTypes = {ClientRequestFilter.class, ClientResponseFilter.class,
                MessageBodyReader.class, MessageBodyWriter.class, ReaderInterceptor.class, WriterInterceptor.class,
                ResponseExceptionMapper.class, ParamConverterProvider.class};
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(MultiTypedProvider.class, providerTypes);
        Configuration configuration = builder.getConfiguration();
        assertTrue(configuration.isRegistered(MultiTypedProvider.class),
                MultiTypedProvider.class + " should be registered");
        assertEquals(configuration.getContracts(MultiTypedProvider.class).size(), providerTypes.length,
                "There should be " + providerTypes.length + " provider types registered");
    }

    @Test
    public void shouldRegisterAMultiTypedProviderClassWithPriorities() {
        Map<Class<?>, Integer> priorities = new HashMap<>();
        priorities.put(ClientRequestFilter.class, 500);
        priorities.put(ClientResponseFilter.class, 501);
        priorities.put(MessageBodyReader.class, 502);
        priorities.put(MessageBodyWriter.class, 503);
        priorities.put(ReaderInterceptor.class, 504);
        priorities.put(WriterInterceptor.class, 505);
        priorities.put(ResponseExceptionMapper.class, 506);
        priorities.put(ParamConverterProvider.class, 507);
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(MultiTypedProvider.class, priorities);
        Configuration configuration = builder.getConfiguration();
        assertTrue(configuration.isRegistered(MultiTypedProvider.class),
                MultiTypedProvider.class + " should be registered");
        Map<Class<?>, Integer> contracts = configuration.getContracts(MultiTypedProvider.class);
        assertEquals(contracts.size(), priorities.size(),
                "There should be " + priorities.size() + " provider types registered");
        for (Map.Entry<Class<?>, Integer> priority : priorities.entrySet()) {
            Integer contractPriority = contracts.get(priority.getKey());
            assertEquals(contractPriority, priority.getValue(),
                    "The priority for " + priority.getKey() + " should be " + priority.getValue());
        }
    }

    @Test
    public void testPropertiesRegistered() {
        String key = "key";
        Object value = new Object();
        RestClientBuilder builder = RestClientBuilder.newBuilder().property(key, value);
        Configuration configuration = builder.getConfiguration();
        assertTrue(configuration.getPropertyNames().contains(key), "The key " + key + " should be a property");
        assertEquals(configuration.getProperty(key), value, "The value of " + key + " should be " + value);
    }
}
