/*
 * Copyright 2024 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.tck.cditests;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.cdi.scoped.AutoCloseableClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.cdi.scoped.CloseableClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.cdi.scoped.StringClient;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWith200RequestFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Tests that clients are closed when destroyed by the CDI container.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ClientClosedTest extends Arquillian {

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, ClientClosedTest.class.getSimpleName() + ".war")
                .addClasses(ReturnWith200RequestFilter.class, AutoCloseableClient.class,
                        CloseableClient.class, StringClient.class, RestActivator.class)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    BeanManager beanManager;

    /**
     * Tests that a client that does not explicitly extend {@link java.io.Closeable} or {@link AutoCloseable} is closed
     * when the CDI bean is destroyed.
     */
    @Test
    public void stringClosed() {
        checkClient(StringClient.class);
    }

    /**
     * Test that a client which extends {@link AutoCloseable} is closed when the CDI bean is destroyed.
     */
    @Test
    public void autoCloseableClosed() {
        checkClient(AutoCloseableClient.class);
    }

    /**
     * Test that a client which extends {@link java.io.Closeable} is closed when the CDI bean is destroyed.
     */
    @Test
    public void closeableClosed() {
        checkClient(CloseableClient.class);
    }

    private <T extends StringClient> void checkClient(final Class<T> type) {
        final Bean<T> resolved = lookup(type);
        final CreationalContext<T> ctx = beanManager.createCreationalContext(resolved);
        final T client = resolved.create(ctx);
        Assert.assertNotNull(client);
        // Assert the response that the client works
        Assert.assertTrue(client.executeGet().startsWith("GET: "));
        resolved.destroy(client, ctx);
        // The bean has been destroyed, expect an IllegalStateException if the method is invoked per the specification
        Assert.expectThrows("Expected an IllegalStateException to be thrown", IllegalStateException.class,
                client::executeGet);
    }

    @SuppressWarnings("unchecked")
    private <T> Bean<T> lookup(final Class<T> type) {
        return (Bean<T>) beanManager.resolve(beanManager.getBeans(type, RestClient.LITERAL));
    }

    @ApplicationPath("/")
    public static class RestActivator extends Application {
    }

}
