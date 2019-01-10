/*
 * Copyright (c) 2019 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.eclipse.microprofile.rest.client.ext.DefaultClientHeadersFactoryImpl;

/**
 * Used to specify that a {@link ClientHeadersFactory} should be used to generate or propagate HTTP headers on the outbound request.
 * When annotation is placed at the interface level of a Rest Client interface, the implementation will invoke the ClientHeadersFactory's
 * <code>update</code> method.
 * <p>
 * If no implementation class of the ClientHeadersFactory interface is specified in the annotation, then the
 * {@link DefaultClientHeadersFactoryImpl} will be used.  This implementation will simply propagate headers (specified via MP Config property)
 * from an inbound JAX-RS request (if applicable) to the outbound request.
 * <p>
 * If a ClientHeadersFactory class specified is not found on the classpath, this should be considered a deployment exception.
 *
 * @since 1.2
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisterClientHeaders {
    /**
     * @return the provider class to register on this client interface
     */
    Class<? extends ClientHeadersFactory> value() default DefaultClientHeadersFactoryImpl.class;
}
