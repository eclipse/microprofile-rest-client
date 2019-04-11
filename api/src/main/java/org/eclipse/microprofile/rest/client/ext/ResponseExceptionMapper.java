/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.ext;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * Converts an JAX-RS Response object into an Exception.
 *
 */
public interface ResponseExceptionMapper<T extends Throwable> {
    int DEFAULT_PRIORITY = Priorities.USER;

    /**
     * Converts a given Response into a Throwable.  The runtime will throw this if it is non-null
     * AND if it is possible to throw given the client method's signature.
     *
     * If this method reads the response body as a stream it must ensure that it resets the stream.
     *
     * @param response the JAX-RS response processed from the underlying client
     * @return A throwable, if this mapper could convert the response.
     */
    T toThrowable(Response response);

    /**
     * Whether or not this mapper will be used for the given response.  By default, any response code of 400 or higher will be handled.
     * Individual mappers may override this method if they want to more narrowly focus on certain response codes or headers.
     *
     * @param status the response status code indicating the HTTP response
     * @param headers the headers from the HTTP response
     * @return whether or not this mapper can convert the Response to a Throwable
     */
    default boolean handles(int status, MultivaluedMap<String, Object> headers) {
        return status >= 400;
    }

    /**
     * The priority of this mapper.  By default, it will use the {@link Priority} annotation's value as the priority.
     * If no annotation is present, it uses a default priority of {@link Priorities#USER}.
     * @return the priority of this mapper
     */
    default int getPriority() {
        return Optional.ofNullable(getClass().getAnnotation(Priority.class))
            .map(Priority::value)
            .orElse(DEFAULT_PRIORITY);
    }
}
