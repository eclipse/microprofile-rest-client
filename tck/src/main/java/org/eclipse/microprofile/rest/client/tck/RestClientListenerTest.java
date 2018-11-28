/*
 * Copyright (c) 2018 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.tck;

import static org.testng.Assert.assertEquals;

import java.net.URI;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.spi.RestClientListener;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWith200RequestFilter;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWith500RequestFilter;
import org.eclipse.microprofile.rest.client.tck.spi.SimpleRestClientListenerImpl;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

/**
 *
 */
public class RestClientListenerTest extends Arquillian {

    @Deployment
    public static WebArchive createDeployment() {
        StringAsset serviceFile = new StringAsset(SimpleRestClientListenerImpl.class.getName());
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
            .addClasses(SimpleGetApi.class,
                        SimpleRestClientListenerImpl.class,
                        ReturnWith200RequestFilter.class,
                        ReturnWith500RequestFilter.class)
            .addAsManifestResource(serviceFile,"services/" + RestClientListener.class.getName());
        return ShrinkWrap.create(WebArchive.class, RestClientListenerTest.class.getSimpleName()+".war")
            .addAsLibrary(jar)
            .addClasses(RestClientListenerTest.class);
    }

    /**
     * This test checks that a RestClientListener loaded via the service loader
     * is invoked.  The RestClientListener impl used will register a
     * ClientRequestFilter that aborts with a 500 status code - it is registered
     * with priority 1.  The test class registers another filter that will abort
     * with a 200 status code, but at priority 2.  If the RestClientListener impl
     * is correctly invoked, then the request will abort with the 500; if not,
     * it will abort with the 200.  This test will also check that the correct
     * serviceInterface class was passed to the RestClientListener impl.
     *
     * @throws Exception - indicates test failure
     */
    @Test
    public void testRestClientListenerInvoked() throws Exception {
        SimpleGetApi client = RestClientBuilder.newBuilder()
            .register(ReturnWith200RequestFilter.class, 2)
            .property("microprofile.rest.client.disable.default.mapper", true)
            .baseUri(new URI("http://localhost:8080/neverUsed"))
            .build(SimpleGetApi.class);

        assertEquals(client.executeGet().getStatus(), 500,
            "The RestClientListener impl was not invoked");
        assertEquals(SimpleRestClientListenerImpl.getServiceInterface(), SimpleGetApi.class,
            "An incorrect serviceInterface class was passed to the RestClientListener impl");
    }
}
