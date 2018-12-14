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

package org.eclipse.microprofile.rest.client.tck.cditests;

import static org.testng.Assert.assertEquals;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.ClientWithURIAndInterceptor;
import org.eclipse.microprofile.rest.client.tck.interfaces.Loggable;
import org.eclipse.microprofile.rest.client.tck.interfaces.LoggableInterceptor;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithURLRequestFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

/**
 * Verifies that CDI interceptors bound to client interface methods are invoked.
 */
public class CDIInterceptorTest extends Arquillian {

    @Inject
    @RestClient
    private ClientWithURIAndInterceptor client;

    @Deployment
    public static WebArchive createDeployment() {
        String simpleName = CDIInterceptorTest.class.getSimpleName();
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, simpleName + ".jar")
            .addClasses(ClientWithURIAndInterceptor.class,
                        Loggable.class,
                        LoggableInterceptor.class,
                        ReturnWithURLRequestFilter.class)
            .addAsManifestResource(new StringAsset(
                "<beans xmlns=\"http://java.sun.com/xml/ns/javaee\"" +
                "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                "       xsi:schemaLocation=\"" +
                "          http://java.sun.com/xml/ns/javaee" +
                "          http://java.sun.com/xml/ns/javaee/beans_1_0.xsd\">" +
                "       <interceptors>" +
                "           <class>org.eclipse.microprofile.rest.client.tck.interfaces.LoggableInterceptor</class>" +
                "       </interceptors>" +
                "</beans>"),
                "beans.xml");
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
            .addAsLibrary(jar)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testInterceptorInvoked() throws Exception {
        LoggableInterceptor.setInvocationMessage("");
        String expectedResponse = "GET http://localhost:5017/myBaseUri/hello";
        assertEquals(client.get(), expectedResponse);

        assertEquals(LoggableInterceptor.getInvocationMessage(),
            ClientWithURIAndInterceptor.class.getName() + ".get " + expectedResponse);
    }

    @Test
    public void testInterceptorNotInvokedWhenNoAnnotationApplied() throws Exception {
        LoggableInterceptor.setInvocationMessage("");
        String expectedResponse = "GET http://localhost:5017/myBaseUri/hello";
        assertEquals(client.getNoInterceptor(), expectedResponse);

        assertEquals(LoggableInterceptor.getInvocationMessage(), "");
    }
}
