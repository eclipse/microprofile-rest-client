/*
 * Copyright 2017-2019 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Stereotype;

/**
 * A marker annotation to register a rest client at runtime.  This marker must be applied to any CDI managed
 * clients.
 *
 * Note that the annotated interface indicates a service-centric view.  Thus users would invoke methods on
 * this interface as if it were running in the same VM as the remote service.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Stereotype
@Dependent
public @interface RegisterRestClient {
    /**
     * Sets the base URI for the rest client interface. This value will be used for the URI unless it is
     * overridden by MicroProfile Config.
     * 
     * @return the base URI for annotated client interface. An empty value indicates that the base URI must be specified
     *  in MicroprofileConfig.
     * @since 1.2
     */
    String baseUri() default "";

    /**
     * Associates the annotated rest client interface with this configuration key. By specifying a non-empty value,
     * this interface can be configured more simply using the configuration key rather than the fully-qualified class
     * name of the interface.
     *
     * @return the configuration key in use by this client interface. An empty value means that this interface is not
     *  associated with a configuration key.
     * @since 1.3
     */
    String configKey() default "";
}
