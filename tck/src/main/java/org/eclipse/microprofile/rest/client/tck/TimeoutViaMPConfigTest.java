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

import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.testng.Assert.assertTrue;

public class TimeoutViaMPConfigTest extends TimeoutTestBase {

    @Deployment
    public static Archive<?> createDeployment() {
        String clientName = SimpleGetApi.class.getName();
        String timeoutProps = clientName + "/mp-rest/connectTimeout=7000" +
                              System.lineSeparator() +
                              clientName + "/mp-rest/readTimeout=7000";
        StringAsset mpConfig = new StringAsset(timeoutProps);
        return ShrinkWrap.create(WebArchive.class, TimeoutViaMPConfigTest.class.getSimpleName()+".war")
            .addAsWebInfResource(mpConfig, "classes/META-INF/microprofile-config.properties")
            .addClasses(SimpleGetApi.class,
                        TimeoutTest.class,
                        TimeoutTestBase.class,
                        WiremockArquillianTest.class);
    }

    @Override
    protected void checkTimeElapsed(long elapsed) {
        assertTrue(elapsed >= 7);
        // allow an extra 10 seconds cushion for slower test machines
        assertTrue(elapsed < 17);
    }
}
