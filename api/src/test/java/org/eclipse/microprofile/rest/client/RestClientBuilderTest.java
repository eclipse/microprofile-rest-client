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

import static org.testng.Assert.assertTrue;

import org.eclipse.microprofile.rest.client.spi.RestClientBuilder1Resolver;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderResolver;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RestClientBuilderTest {

    @BeforeMethod
    public void cleanupResolver() {
        RestClientBuilderResolver.setInstance(null);
    }

    @Test
    public void testGetHighestService() {
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        assertTrue(builder instanceof BuilderImpl2);
    }

    @Test
    public void testGetBuilderFromDynamicallyRegistered() {
        // given
        RestClientBuilderResolver.setInstance(new RestClientBuilder1Resolver());
        // when
        RestClientBuilder builder = RestClientBuilder.newBuilder();
        // then
        assertTrue(builder instanceof BuilderImpl1);
    }

}
