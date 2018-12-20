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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.fail;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.ProcessingException;

import org.eclipse.microprofile.rest.client.tck.WiremockArquillianTest;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.testng.annotations.Test;





public abstract class TimeoutTestBase extends WiremockArquillianTest {

    protected static final String UNUSED_URL =
        AccessController.doPrivileged((PrivilegedAction<String>) () ->
            System.getProperty(
                "org.eclipse.microprofile.rest.client.tck.unusedURL",
                "http://microprofile.io:1234/null")
        );

    @Test(expectedExceptions={ProcessingException.class})
    public void testConnectTimeout() throws Exception {

        long startTime = System.nanoTime();
        try {
            getClientWithConnectTimeout().executeGet();
            fail("A ProcessingException should have been thrown to indicate a timeout");
        }
        finally {
            long elapsedTime = System.nanoTime() - startTime;
            long elapsedSecs = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
            checkTimeElapsed(elapsedSecs);
        }
    }

    @Test(expectedExceptions={ProcessingException.class})
    public void testReadTimeout() throws Exception {

        stubFor(get(urlEqualTo("/")).willReturn(aResponse()
                                        .withStatus(200)
                                        .withFixedDelay(30000)));
        long startTime = System.nanoTime();
        try {
            getClientWithReadTimeout().executeGet();
            fail("A ProcessingException should have been thrown due to a read timeout");
        }
        finally {
            long elapsedTime = System.nanoTime() - startTime;
            long elapsedSecs = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
            checkTimeElapsed(elapsedSecs);
        }
    }

    protected abstract SimpleGetApi getClientWithReadTimeout();
    protected abstract SimpleGetApi getClientWithConnectTimeout();


    protected abstract void checkTimeElapsed(long elapsed);
}
