/*
 * Copyright 2018 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to specify HTTP parameters that should be sent with the outbound request.
 * When this annotation is placed at the interface level of a REST client interface, the specified headers will be sent on each request for all
 * methods in the interface.
 * When this annotation is placed on a method, the headers will be sent only for that method. If the same HTTP header is specified in an annotation
 * for both the type and the method, only the header value specified in the annotation on the method will be sent.
 * <p>
 * This class serves to act as the {@link java.lang.annotation.Repeatable} implementation for {@link ClientHeaderParam}.
 *
 * @since 1.2
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ClientHeaderParams {
    ClientHeaderParam[] value();
}
