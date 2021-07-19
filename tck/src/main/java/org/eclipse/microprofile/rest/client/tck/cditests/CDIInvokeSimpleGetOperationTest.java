/*
 * Copyright 2017, 2021 Contributors to the Eclipse Foundation
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.testng.Assert.assertEquals;

import java.util.Set;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.WiremockArquillianTest;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

/**
 * Verifies via CDI injection that you can use a programmatic interface. verifies that the interface has Dependent
 * scope.
 */
public class CDIInvokeSimpleGetOperationTest extends WiremockArquillianTest {
    @Inject
    @RestClient
    private SimpleGetApi api;
    @Inject
    private BeanManager beanManager;
    @Deployment
    public static WebArchive createDeployment() {
        String propertyName = SimpleGetApi.class.getName() + "/mp-rest/url";
        String value = getStringURL();
        String simpleName = CDIInvokeSimpleGetOperationTest.class.getSimpleName();
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, simpleName + ".jar")
                .addClasses(SimpleGetApi.class, WiremockArquillianTest.class)
                .addAsManifestResource(new StringAsset(propertyName + "=" + value), "microprofile-config.properties")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
                .addAsLibrary(jar)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }
    @Test
    public void testInvokesGetOperationWithCDIBean() throws Exception {
        String expectedBody = "Hello, MicroProfile!";
        stubFor(get(urlEqualTo("/"))
                .willReturn(aResponse()
                        .withBody(expectedBody)));

        Response response = api.executeGet();

        String body = response.readEntity(String.class);

        response.close();

        assertEquals(body, expectedBody);

        verify(1, getRequestedFor(urlEqualTo("/")));
    }

    /**
     * Tests that the component injected has Dependent scope
     */
    @Test
    public void testHasDependentScopedByDefault() {
        Set<Bean<?>> beans = beanManager.getBeans(SimpleGetApi.class, RestClient.LITERAL);
        Bean<?> resolved = beanManager.resolve(beans);
        assertEquals(resolved.getScope(), Dependent.class);
    }
}
