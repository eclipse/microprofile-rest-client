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

import java.net.URI;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.CustomHttpMethod;
import org.eclipse.microprofile.rest.client.tck.providers.CustomHttpMethodFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.Response;

public class CustomHttpMethodTest extends Arquillian {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, CustomHttpMethodTest.class.getSimpleName() + ".war")
                .addPackage(CustomHttpMethodFilter.class.getPackage())
                .addClass(CustomHttpMethod.class);
    }

    @Test
    public void invokesUserDefinedHttpMethod() throws Exception {
        CustomHttpMethodFilter filter = new CustomHttpMethodFilter();
        RestClientBuilder builder = RestClientBuilder.newBuilder().register(filter);
        CustomHttpMethod client = builder.baseUri(new URI("http://localhost/stub")).build(CustomHttpMethod.class);
        Response response = client.executeMyMethod();
        assertEquals(response.getStatus(), 200, "Unexpected HTTP Method sent from client - " +
                "expected \"MYMETHOD\", was \"" + response.readEntity(String.class) + "\"");
    }

}
