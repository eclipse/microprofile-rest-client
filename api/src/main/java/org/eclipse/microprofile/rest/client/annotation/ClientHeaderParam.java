/*
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
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
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to specify an HTTP parameter that should be sent with the outbound request.
 * When this annotation is placed at the interface level of a REST client interface, the specified header will be sent on each request for all
 * methods in the interface.
 * When this annotation is placed on a method, the header will be sent only for that method. If the same HTTP header is specified in an annotation
 * for both the type and the method, only the header value specified in the annotation on the method will be sent.
 * <p>
 * The value of the header to send can be specified explicitly by using the <code>value</code> attribute.
 * The value can also be computed via a default method on the client interface or a public static method on a different class.  The compute method
 * must return a String or String[] (indicating a multivalued header) value.  This method must be specified in the <code>value</code> attribute but
 * wrapped in curly-braces. The compute method's signature must either contain no arguments or a single <code>String</code> argument. The String
 * argument is the name of the header.
 * <p>
 * Here is an example that explicitly defines a header value and computes a value:
 * <pre>
 * public interface MyClient {
 *
 *    static AtomicInteger counter = new AtomicInteger(1);
 *
 *    default String determineHeaderValue(String headerName) {
 *        if ("SomeHeader".equals(headerName)) {
 *            return "InvokedCount " + counter.getAndIncrement();
 *        }
 *        throw new UnsupportedOperationException("unknown header name");
 *    }
 *
 *    {@literal @}ClientHeaderParam(name="SomeHeader", value="ExplicitlyDefinedValue")
 *    {@literal @}GET
 *    Response useExplicitHeaderValue();
 *
 *    {@literal @}ClientHeaderParam(name="SomeHeader", value="{determineHeaderValue}")
 *    {@literal @}DELETE
 *    Response useComputedHeaderValue();
 * }
 * </pre>
 * The implementation should fail to deploy a client interface if the annotation contains a <code>@ClientHeaderParam</code> annotation with a
 * <code>value</code> attribute that references a method that does not exist, or contains an invalid signature.
 * <p>
 * The <code>required</code> attribute will determine what action the implementation should take if the method specified in the <code>value</code>
 * attribute throws an exception. If the attribute is true (default), then the implementation will abort the request and will throw the exception
 * back to the caller. If the <code>required</code> attribute is set to false, then the implementation will not send this header if the method throws
 * an exception.
 * <p>
 * Note that if an interface method contains an argument annotated with <code>@HeaderParam</code>, that argument will take priority over anything
 * specified in a ClientHeaderParam annotation.
 *
 * @since 1.2
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(ClientHeaderParams.class)
public @interface ClientHeaderParam {
    /**
     * @return the name of the HTTP header.
     */
    String name();

    /**
     * @return the value(s) of the HTTP header - or the method to invoke to get the value (surrounded by curly braces).
     */
    String[] value();

    /**
     * @return whether to abort the request if the method to compute the header value throws an exception (true; default) or just skip this header
     * (false)
     */
    boolean required() default true;
}
