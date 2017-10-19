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

@Priority(2)
public class BuilderImpl2 extends RestClientBuilder {
    @Override
    public RestClientBuilder baseUrl(URL url) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public <T> T build(Class<T> clazz) {
        throw new IllegalStateException("not implemented");
    }
}
