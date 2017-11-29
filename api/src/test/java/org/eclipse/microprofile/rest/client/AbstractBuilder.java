/*
 * Copyright 2017 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client;

import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;
import java.util.Map;

public abstract class AbstractBuilder implements RestClientBuilder, Configurable<RestClientBuilder> {
    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public AbstractBuilder property(String s, Object o) {
        return this;
    }

    @Override
    public AbstractBuilder register(Class<?> aClass) {
        return this;
    }

    @Override
    public AbstractBuilder register(Class<?> aClass, int i) {
        return this;
    }

    @Override
    public AbstractBuilder register(Class<?> aClass, Class<?>... classes) {
        return this;
    }

    @Override
    public AbstractBuilder register(Class<?> aClass, Map<Class<?>, Integer> map) {
        return this;
    }

    @Override
    public AbstractBuilder register(Object o) {
        return this;
    }

    @Override
    public AbstractBuilder register(Object o, int i) {
        return this;
    }

    @Override
    public AbstractBuilder register(Object o, Class<?>... classes) {
        return this;
    }

    @Override
    public AbstractBuilder register(Object o, Map<Class<?>, Integer> map) {
        return this;
    }
}
