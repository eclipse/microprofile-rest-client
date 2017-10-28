/*
 * Copyright 2017 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.tck.cditests;

import org.eclipse.microprofile.rest.client.tck.WiremockArquillianTest;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.testng.Assert.assertEquals;

/**
 * Verifies via CDI injection that you can use a programmatic interface.  verifies that the interface has Dependent scope.
 */
public class CDIInvokeSimpleGetOperationTest extends WiremockArquillianTest{
    @Inject
    private SimpleGetApi api;
    @Inject
    private BeanManager beanManager;
    @Deployment
    public static WebArchive createDeployment() {
        String propertyName = SimpleGetApi.class.getName()+"/mp-rest/url";
        String value = "http://localhost:"+ getPort();
        return ShrinkWrap.create(WebArchive.class)
            .addClass(SimpleGetApi.class)
            .addAsManifestResource(new StringAsset(propertyName+"="+value), "microprofile-config.properties")
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testInvokesOperation() throws Exception{
        String expectedBody = "Hello, MicroProfile!";
        getWireMockServer().stubFor(get(urlEqualTo("/"))
            .willReturn(aResponse()
                .withBody(expectedBody)));

        Response response = api.executeGet();

        String body = response.readEntity(String.class);

        response.close();

        assertEquals(body, expectedBody);

        getWireMockServer().verify(1, getRequestedFor(urlEqualTo("/")));
    }

    @Test
    public void testHasDependentScopedByDefault() {
        Set<Bean<?>> beans = beanManager.getBeans(SimpleGetApi.class);
        Bean<?> resolved = beanManager.resolve(beans);
        assertEquals(resolved.getScope(), Dependent.class);
    }
}
