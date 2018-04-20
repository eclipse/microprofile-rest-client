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
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderListener;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWith200RequestFilter;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWith500RequestFilter;
import org.eclipse.microprofile.rest.client.tck.spi.SimpleRestClientBuilderListenerImpl;
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
public class RestClientBuilderListenerTest extends Arquillian {

    @Deployment
    public static WebArchive createDeployment() {
        StringAsset serviceFile = new StringAsset(SimpleRestClientBuilderListenerImpl.class.getName());
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
            .addClasses(SimpleGetApi.class,
                        SimpleRestClientBuilderListenerImpl.class,
                        ReturnWith200RequestFilter.class)
            .addAsManifestResource(serviceFile,"services/" + RestClientBuilderListener.class.getName());
        return ShrinkWrap.create(WebArchive.class, RestClientBuilderListenerTest.class.getSimpleName()+".war")
            .addAsLibrary(jar)
            .addClasses(RestClientBuilderListenerTest.class, ReturnWith500RequestFilter.class);
    }

    /**
     * This test checks that a RestClientBuilderListener loaded via the service loader
     * is invoked.  The RestClientBuilderListener impl used will register a
     * ClientRequestFilter that aborts with a 200 status code - it is registered
     * with priority 1.  The test class registers another filter that will abort
     * with a 500 status code, but at priority 2.  If the RestClientBuilderListener impl
     * is correctly invoked, then the request will abort with the 200; if not,
     * it will abort with the 500.
     *
     * @throws Exception - indicates test failure
     */
    @Test
    public void testRegistrarInvoked() throws Exception {
        SimpleGetApi client = RestClientBuilder.newBuilder()
            .register(ReturnWith500RequestFilter.class, 2)
            .baseUri(new URI("http://localhost:8080/neverUsed"))
            .build(SimpleGetApi.class);

        assertEquals(client.executeGet().getStatus(), 200,
            "The RestClientBuilderListener impl was not invoked");
    }
}
