/*
 * Copyright 2018, 2021 Contributors to the Eclipse Foundation
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
import java.util.logging.Logger;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.ProducesConsumesClient;
import org.eclipse.microprofile.rest.client.tck.providers.ProducesConsumesFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Tests that MP Rest Client's <code>@Produces</code> annotation affects the value transmitted in the
 * <code>Accept</code> header, and that it's <code>@Consumes</code> annotation affects the value transmitted in the
 * <code>Content-Type</code> header. Note that this is opposite of what you would expect for JAX-RS resources.
 */
public class ProducesConsumesTest extends Arquillian {
    private final static Logger LOG = Logger.getLogger(ProducesConsumesTest.class.getName());
    public static final String XML_PAYLOAD = "<some><wrapped>value</wrapped></some>";
    public static final String JSON_PAYLOAD = "{\"some\": \"value\"}";

    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, ProducesConsumesTest.class.getSimpleName() + ".war")
                .addClasses(ProducesConsumesClient.class, ProducesConsumesFilter.class);
    }

    @Test
    public void testProducesConsumesAnnotationOnMethod() {
        final String m = "testProducesConsumesAnnotationOnClientInterface";
        ProducesConsumesClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:8080/null"))
                .register(ProducesConsumesFilter.class)
                .build(ProducesConsumesClient.class);

        LOG.info(m + " @Produce(application/json) @Consume(application/xml)");
        Response r = client.produceJSONConsumeXML(XML_PAYLOAD);
        String acceptHeader = r.getHeaderString("Sent-Accept");
        LOG.info(m + "Sent-Accept: " + acceptHeader);
        String contentTypeHeader = r.getHeaderString("Sent-ContentType");
        LOG.info(m + "Sent-ContentType: " + contentTypeHeader);
        assertEquals(acceptHeader, MediaType.APPLICATION_JSON);
        assertEquals(contentTypeHeader, MediaType.APPLICATION_XML);

        LOG.info(m + " @Produce(application/xml) @Consume(application/json)");
        r = client.produceXMLConsumeJSON(JSON_PAYLOAD);
        acceptHeader = r.getHeaderString("Sent-Accept");
        LOG.info(m + "Sent-Accept: " + acceptHeader);
        contentTypeHeader = r.getHeaderString("Sent-ContentType");
        LOG.info(m + "Sent-ContentType: " + contentTypeHeader);
        assertEquals(acceptHeader, MediaType.APPLICATION_XML);
        assertEquals(contentTypeHeader, MediaType.APPLICATION_JSON);
    }

    @Test
    public void testProducesConsumesAnnotationOnInterface() {
        final String m = "testProducesConsumesAnnotationOnInterface";
        ProducesConsumesClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:8080/null"))
                .register(ProducesConsumesFilter.class)
                .build(ProducesConsumesClient.class);

        LOG.info(m + " @Produce(text/html) @Consume(text/plain)");
        Response r = client.produceHtmlConsumeText("1", "whatever");
        String acceptHeader = r.getHeaderString("Sent-Accept");
        LOG.info(m + "Sent-Accept: " + acceptHeader);
        String contentTypeHeader = r.getHeaderString("Sent-ContentType");
        LOG.info(m + "Sent-ContentType: " + contentTypeHeader);
        assertEquals(acceptHeader, MediaType.TEXT_HTML);
        assertEquals(contentTypeHeader, MediaType.TEXT_PLAIN);
    }
}
