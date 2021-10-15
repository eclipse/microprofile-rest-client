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
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

/**
 * This test verifies that even if a client is configured via MP Config, that if a separate client instance is created
 * programmatically and using a different timeout, then the programmatically specified timeout is honored.
 */
public class TimeoutBuilderIndependentOfMPConfigTest extends TimeoutTestBase {

    private static final int MP_CONFIG_TIMEOUT = 15000;
    private static final int PROGRAMMATIC_TIMEOUT = 5000;

    @Deployment
    public static Archive<?> createDeployment() {
        String clientName = SimpleGetApi.class.getName();
        String timeoutProps = clientName + "/mp-rest/connectTimeout=" + MP_CONFIG_TIMEOUT +
                System.lineSeparator() +
                clientName + "/mp-rest/readTimeout=" + MP_CONFIG_TIMEOUT;
        StringAsset mpConfig = new StringAsset(timeoutProps);
        return ShrinkWrap
                .create(WebArchive.class, TimeoutBuilderIndependentOfMPConfigTest.class.getSimpleName() + ".war")
                .addAsWebInfResource(mpConfig, "classes/META-INF/microprofile-config.properties")
                .addClasses(SimpleGetApi.class,
                        TimeoutTestBase.class,
                        WiremockArquillianTest.class);
    }

    @Override
    protected SimpleGetApi getClientWithReadTimeout() {
        return RestClientBuilder.newBuilder()
                .baseUri(WiremockArquillianTest.getServerURI())
                .readTimeout(PROGRAMMATIC_TIMEOUT, TimeUnit.MILLISECONDS)
                .build(SimpleGetApi.class);
    }

    @Override
    protected SimpleGetApi getClientWithConnectTimeout() {
        return RestClientBuilder.newBuilder()
                .baseUri(URI.create(UNUSED_URL))
                .connectTimeout(PROGRAMMATIC_TIMEOUT, TimeUnit.MILLISECONDS)
                .build(SimpleGetApi.class);
    }

    @Override
    protected void checkTimeElapsed(long elapsed) {

        assertTrue(elapsed >= PROGRAMMATIC_TIMEOUT - ROUNDING_FACTOR_CUSHION);
        // allow extra seconds cushion for slower test machines
        final long elapsedLimit = PROGRAMMATIC_TIMEOUT + TIMEOUT_CUSHION;
        assertTrue(elapsed < elapsedLimit,
                "Elapsed time expected under " + elapsedLimit + "ms, but was " + elapsed + "ms.");
    }
}
