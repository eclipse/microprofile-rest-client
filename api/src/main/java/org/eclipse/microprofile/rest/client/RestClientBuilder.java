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

import javax.annotation.Priority;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

public abstract class RestClientBuilder {
    public static RestClientBuilder newBuilder() {
        ServiceLoader<RestClientBuilder> loader = ServiceLoader.load(RestClientBuilder.class);
        List<RestClientBuilder> clientBuilders = new ArrayList<>();
        loader.forEach(clientBuilders::add);
        loader = ServiceLoader.load(RestClientBuilder.class, RestClientBuilder.class.getClassLoader());
        loader.forEach(clientBuilders::add);

        if(clientBuilders.size() == 0) {
            throw new RuntimeException("No implementation of '"+RestClientBuilder.class.getSimpleName()+"' found");
        }
        clientBuilders.sort(Comparator.comparingInt(value -> {
            Priority priority = value.getClass().getAnnotation(Priority.class);
            if (priority == null) {
                return 1;
            }
            else {
                return priority.value();
            }
        }).reversed());
        return clientBuilders.get(0);
    }

    public abstract RestClientBuilder baseUrl(URL url);

    public abstract <T> T build(Class<T> clazz);
}
