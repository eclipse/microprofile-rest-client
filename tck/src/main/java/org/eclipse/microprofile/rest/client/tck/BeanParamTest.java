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

package org.eclipse.microprofile.rest.client.tck;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.InterfaceUsingBeanParam;
import org.eclipse.microprofile.rest.client.tck.interfaces.MyBean;
import org.eclipse.microprofile.rest.client.tck.providers.BeanParamFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.Response;

public class BeanParamTest extends Arquillian {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, BeanParamTest.class.getSimpleName() + ".war")
                .addPackage(BeanParamFilter.class.getPackage())
                .addClasses(InterfaceUsingBeanParam.class, MyBean.class);
    }

    @Test
    public void sendsParamsSpecifiedInBeanParam() throws Exception {
        BeanParamFilter filter = new BeanParamFilter();
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(filter);
        InterfaceUsingBeanParam client =
                builder.baseUri(new URI("http://localhost/stub")).build(InterfaceUsingBeanParam.class);

        MyBean myBean = new MyBean("qParam", "123", "headerVal");
        Response response = client.executePut(myBean, "body");
        assertEquals(response.getStatus(), 200, "Unexpected response - filter not properly registered");
        String responseStr = response.readEntity(String.class);
        assertNotNull(responseStr, "Null entity returned from filter/server");
        assertTrue(responseStr.contains("qParam"), "QueryParam value not sent in request");
        assertTrue(responseStr.contains("123"), "CookieParam value not sent in request");
        assertTrue(responseStr.contains("headerVal"), "QueryParam value not sent in request");
        assertTrue(responseStr.contains("body"), "Body not sent in request");

        myBean.setCookie("456");
        response = client.executePut(myBean, "body");
        assertEquals(response.getStatus(), 200, "Unexpected response - filter not properly registered");
        responseStr = response.readEntity(String.class);
        assertNotNull(responseStr, "Null entity returned from filter/server");
        assertTrue(responseStr.contains("qParam"), "QueryParam value not sent in second request");
        assertTrue(responseStr.contains("456"), "CookieParam value not sent in second request");
        assertTrue(responseStr.contains("headerVal"), "QueryParam value not sent in second request");
        assertTrue(responseStr.contains("body"), "Body not sent in second request");
    }

}
