/*
 * Copyright 2019 Contributors to the Eclipse Foundation
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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.StringClient;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithSpecifiedHeaderFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

public class DefaultMIMETypeTest extends Arquillian {

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, DefaultMIMETypeTest.class.getSimpleName()+".war")
            .addClasses(DefaultMIMETypeTest.class, StringClient.class, ReturnWithSpecifiedHeaderFilter.class);
    }

    @Test
    public void testDefaultMIMETypeIsApplicationJson_Accept() throws Exception {
        assertEquals(client().getHeaderValue(HttpHeaders.ACCEPT), MediaType.APPLICATION_JSON);
    }

    @Test
    public void testDefaultMIMETypeIsApplicationJson_ContentType() throws Exception {
        assertEquals(client().getHeaderValue(HttpHeaders.CONTENT_TYPE), MediaType.APPLICATION_JSON);
    }

    private StringClient client() {
        return RestClientBuilder.newBuilder()
            .baseUri(URI.create("http://localhost:1234/notUsed"))
            .register(ReturnWithSpecifiedHeaderFilter.class)
            .build(StringClient.class);
    }
}
