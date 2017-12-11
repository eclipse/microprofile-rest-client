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
package org.eclipse.microprofile.rest.client;

/**
 * This exception is thrown when the MicroProfile Rest Client implementation
 * attempts to build a client using an invalid interface.  Interfaces are
 * considered invalid (1) if it contains a method with more than one HTTP method
 * annotations or (2) if the combined type-level/method-level URI path contains
 * an unresolved URI template or (3) if an interface method contains a
 * <code>@PathParam</code> annotation that refers to a URI template that is not
 * defined in any <code>@Path</code> annotations on the method or interface.
 */
public class RestClientDefinitionException extends RuntimeException {
    static final long serialVersionUID = -3544786190345722935L;

    public RestClientDefinitionException() {}

    public RestClientDefinitionException(String paramString) {
        super(paramString);
    }

    public RestClientDefinitionException(String paramString, Throwable paramThrowable) {
        super(paramString, paramThrowable);
    }

    public RestClientDefinitionException(Throwable paramThrowable) {
        super(paramThrowable);
    }
}
