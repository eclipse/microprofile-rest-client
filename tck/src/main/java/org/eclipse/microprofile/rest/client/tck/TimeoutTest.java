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

package org.eclipse.microprofile.rest.client.tck;

import static org.testng.Assert.assertTrue;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;

public class TimeoutTest extends TimeoutTestBase {

    private static final int UNUSED_PORT =
        AccessController.doPrivileged((PrivilegedAction<Integer>) () -> {
            return Integer.getInteger(
                "org.eclipse.microprofile.rest.client.tck.unusedPort", 23);
        });

    @Deployment
    public static Archive<?> createDeployment() {
        String simpleName = TimeoutTest.class.getSimpleName();
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
                         .addClasses(WiremockArquillianTest.class,
                                     TimeoutTestBase.class,
                                     SimpleGetApi.class);
    }

    @Override
    protected void checkTimeElapsed(long min, long max, long elapsed) {
        assertTrue(elapsed >= 5);
        // allow an extra 10 seconds cushion for slower test machines
        assertTrue(elapsed < 15);
    }
}
