/*
 *******************************************************************************
 * Copyright (c) 2016-2019 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.eclipse.microprofile.rest.client.spi;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ServiceLoader;

/**
 * Resolver for a {@link RestClientBuilder} implementation. A resolver should
 * extend this class and and be registered via the
 * {@link java.util.ServiceLoader} mechanism or via
 * {@link #setInstance(RestClientBuilderResolver resolver)}.
 * <p>
 * This class is not intended to be used by end-users but for portable
 * integration purpose only to provide implementation of
 * <code>RestClientBuilder</code> instances.
 * <p>
 * Implementations have to provide the {@link #newBuilder()} method to create custom
 * <code>RestClientBuilder</code> implementations.
 *
 * @author Ondrej Mihalyi
 * @author John D. Ament
 */
public abstract class RestClientBuilderResolver {

    private static volatile RestClientBuilderResolver instance = null;

    protected RestClientBuilderResolver() {
    }

    /**
     * Creates a new RestClientBuilder instance.
     * <p>
     * Implementations are expected to override the {@link #newBuilder()} method
     * to create custom RestClientBuilder implementations.
     * <p>
     * @return new RestClientBuilder instance
     */
    public abstract RestClientBuilder newBuilder();

    /**
     * Gets or creates a RestClientBuilderResolver instance. Only used
     * internally from within {@link RestClientBuilder}
     *
     * @return an instance of RestClientBuilderResolver
     */
    // method copied and adapted from ConfigProviderResolver in microprofile-config
    public static RestClientBuilderResolver instance() {
        if (instance == null) {
            synchronized (RestClientBuilderResolver.class) {
                if (instance != null) {
                    return instance;
                }
                PrivilegedAction<ClassLoader> action = () -> Thread.currentThread().getContextClassLoader();
                ClassLoader cl = AccessController.doPrivileged(action);
                if (cl == null) {
                    action = () -> RestClientBuilderResolver.class.getClassLoader();
                    cl = AccessController.doPrivileged(action);
                }

                RestClientBuilderResolver newInstance = loadSpi(cl);

                if (newInstance == null) {
                    throw new IllegalStateException(
                            "No RestClientBuilderResolver implementation found!");
                }

                instance = newInstance;
            }
        }

        return instance;
    }

    // method copied and adapted from ConfigProviderResolver in microprofile-config
    private static RestClientBuilderResolver loadSpi(ClassLoader cl) {
        if (cl == null) {
            return null;
        }

        RestClientBuilderResolver instance = null;

        ServiceLoader<RestClientBuilderResolver> sl = ServiceLoader.load(RestClientBuilderResolver.class, cl);
        for (RestClientBuilderResolver spi : sl) {
            if (instance != null) {
                throw new IllegalStateException("Multiple RestClientBuilderResolver implementations found: "
                                                + spi.getClass().getName() + " and "
                                                + instance.getClass().getName());
            }
            instance = spi;
        }

        return instance;
    }

    /**
     * Set the instance. It can be as an alternative to service loader pattern,
     * e.g. in OSGi environment
     *
     * @param resolver instance.
     */
    public static void setInstance(RestClientBuilderResolver resolver) {
        instance = resolver;
    }

}
