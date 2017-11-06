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
import javax.ws.rs.core.Response;
import java.util.Optional;

/**
 * Converts an JAX-RS Response object into an Exception.
 *
 */
public interface ResponseExceptionMapper {
    int DEFAULT_PRIORITY = 100;

    /**
     * Converts a given Response into a Throwable.  The runtime will throw this if it is present.
     *
     * If this method reads the response body as a stream it must ensure that it resets the stream.
     *
     * @param response the JAX-RS response processed from the underlying client
     * @return A throwable, if this mapper could convert the response.
     */
    Optional<Throwable> toThrowable(Response response);

    /**
     * Whether or not this mapper will be used for the given response.  By default, any response code of 400 or higher will be handled.
     * Individual mappers may override this method if they want to more narrowly focus on certain response codes.
     * 
     * If this method reads the response body as a stream it must ensure that it resets the stream.
     *
     * @param response
     * @return
     */
    default boolean handles(Response response) {
        return response.getStatus() >= 400;
    }

    /**
     * The priority of this mapper.  By default, it will use the {@link Priority} annotation's value as the priority.
     * If no annotation is present, it uses a default priority of 100.
     * @return
     */
    default int getPriority() {
        Priority priority = getClass().getAnnotation(Priority.class);
        if(priority == null) {
            return DEFAULT_PRIORITY;
        }
        return priority.value();
    }
}
