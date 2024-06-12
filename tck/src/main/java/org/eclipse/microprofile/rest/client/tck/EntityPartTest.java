/*
 * Copyright 2024 Contributors to the Eclipse Foundation
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.Assert;
import org.testng.annotations.Test;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@RunAsClient
public class EntityPartTest extends Arquillian {

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, EntityPart.class.getSimpleName() + ".war")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    /**
     * Tests that a single file is upload. The response is a simple JSON response with the file information.
     *
     * @throws Exception
     *             if a test error occurs
     */
    @Test
    public void uploadFile() throws Exception {
        try (FileManagerClient client = createClient()) {
            final byte[] content;
            try (InputStream in = EntityPartTest.class.getResourceAsStream("/multipart/test-file1.txt")) {
                Assert.assertNotNull(in, "Could not find /multipart/test-file1.txt");
                content = in.readAllBytes();
            }
            // Send in an InputStream to ensure it works with an InputStream
            final List<EntityPart> files = List.of(EntityPart.withFileName("test-file1.txt")
                    .content(new ByteArrayInputStream(content))
                    .mediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                    .build());
            try (Response response = client.uploadFile(files)) {
                Assert.assertEquals(201, response.getStatus());
                final JsonArray jsonArray = response.readEntity(JsonArray.class);
                Assert.assertNotNull(jsonArray);
                Assert.assertEquals(jsonArray.size(), 1);
                final JsonObject json = jsonArray.getJsonObject(0);
                Assert.assertEquals(json.getString("name"), "test-file1.txt");
                Assert.assertEquals(json.getString("fileName"), "test-file1.txt");
                Assert.assertEquals(json.getString("content"), "This is a test file for file 1.\n");
            }
        }
    }

    /**
     * Tests that two files are upload. The response is a simple JSON response with the file information.
     *
     * @throws Exception
     *             if a test error occurs
     */
    @Test
    public void uploadMultipleFiles() throws Exception {
        try (FileManagerClient client = createClient()) {
            final Map<String, byte[]> entityPartContent = new LinkedHashMap<>(2);
            try (InputStream in = EntityPartTest.class.getResourceAsStream("/multipart/test-file1.txt")) {
                Assert.assertNotNull(in, "Could not find /multipart/test-file1.txt");
                entityPartContent.put("test-file1.txt", in.readAllBytes());
            }
            try (InputStream in = EntityPartTest.class.getResourceAsStream("/multipart/test-file2.txt")) {
                Assert.assertNotNull(in, "Could not find /multipart/test-file2.txt");
                entityPartContent.put("test-file2.txt", in.readAllBytes());
            }
            final List<EntityPart> files = entityPartContent.entrySet()
                    .stream()
                    .map((entry) -> {
                        try {
                            return EntityPart.withName(entry.getKey())
                                    .fileName(entry.getKey())
                                    .content(entry.getValue())
                                    .mediaType(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                                    .build();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .collect(Collectors.toList());

            try (Response response = client.uploadFile(files)) {
                Assert.assertEquals(201, response.getStatus());
                final JsonArray jsonArray = response.readEntity(JsonArray.class);
                Assert.assertNotNull(jsonArray);
                Assert.assertEquals(jsonArray.size(), 2);
                // Don't assume the results are in a specific order
                for (JsonValue value : jsonArray) {
                    final JsonObject json = value.asJsonObject();
                    if (json.getString("name").equals("test-file1.txt")) {
                        Assert.assertEquals(json.getString("fileName"), "test-file1.txt");
                        Assert.assertEquals(json.getString("content"), "This is a test file for file 1.\n");
                    } else if (json.getString("name").equals("test-file2.txt")) {
                        Assert.assertEquals(json.getString("fileName"), "test-file2.txt");
                        Assert.assertEquals(json.getString("content"), "This is a test file for file 2.\n");
                    } else {
                        Assert.fail(String.format("Unexpected entry %s in JSON response: %n%s", json, jsonArray));
                    }
                }
            }
        }
    }

    private static FileManagerClient createClient() {
        return RestClientBuilder.newBuilder()
                // Fake URI as we use a filter to short-circuit the request
                .baseUri("http://localhost:8080")
                .register(new FileManagerFilter())
                .build(FileManagerClient.class);
    }

    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.MULTIPART_FORM_DATA)
    public interface FileManagerClient extends AutoCloseable {

        @POST
        @Path("upload")
        Response uploadFile(List<EntityPart> entityParts) throws IOException;
    }

    public static class FileManagerFilter implements ClientRequestFilter {

        @Override
        public void filter(final ClientRequestContext requestContext) throws IOException {
            if (requestContext.getMethod().equals("POST")) {
                // Download the file
                @SuppressWarnings("unchecked")
                final List<EntityPart> entityParts = (List<EntityPart>) requestContext.getEntity();
                final JsonArrayBuilder jsonBuilder = Json.createArrayBuilder();
                for (EntityPart part : entityParts) {
                    final JsonObjectBuilder jsonPartBuilder = Json.createObjectBuilder();
                    jsonPartBuilder.add("name", part.getName());
                    if (part.getFileName().isPresent()) {
                        jsonPartBuilder.add("fileName", part.getFileName().get());
                    } else {
                        throw new BadRequestException("No file name for entity part " + part);
                    }
                    jsonPartBuilder.add("content", part.getContent(String.class));
                    jsonBuilder.add(jsonPartBuilder);
                }
                requestContext.abortWith(Response.status(201).entity(jsonBuilder.build()).build());
            } else {
                requestContext
                        .abortWith(Response.status(Response.Status.BAD_REQUEST).entity("Invalid request").build());
            }
        }
    }
}
