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

package org.eclipse.microprofile.rest.client.tck.timeout;

import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.WiremockArquillianTest;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class TimeoutTest extends TimeoutTestBase {

    @Deployment
    public static Archive<?> createDeployment() {
        String simpleName = TimeoutTest.class.getSimpleName();
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
                         .addClasses(WiremockArquillianTest.class,
                                     TimeoutTestBase.class,
                                     SimpleGetApi.class);
    }

    @Override
    protected SimpleGetApi getClientWithReadTimeout() {
        return RestClientBuilder.newBuilder()
            .baseUri(WiremockArquillianTest.getServerURI())
            .readTimeout(5, TimeUnit.SECONDS)
            .build(SimpleGetApi.class);
    }

    @Override
    protected SimpleGetApi getClientWithConnectTimeout() {
        return RestClientBuilder.newBuilder()
            .baseUri(URI.create(UNUSED_URL))
            .connectTimeout(5, TimeUnit.SECONDS)
            .build(SimpleGetApi.class);
    }

    @Override
    protected void checkTimeElapsed(long elapsed) {
        assertTrue(elapsed >= 5);
        // allow an extra 10 seconds cushion for slower test machines
        assertTrue(elapsed < 15);
    }
}
