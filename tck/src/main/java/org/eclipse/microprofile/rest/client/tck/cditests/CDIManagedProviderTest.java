/*
 * Copyright 2020, 2021 Contributors to the Eclipse Foundation
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

import java.io.IOException;
import java.net.URI;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.Response;

/**
 * Verifies the CDI-managed providers are used when their class is registered with the client interface.
 */
public class CDIManagedProviderTest extends Arquillian {
    private static final String STUB_URI = "http://localhost:9080/stub";

    @Inject
    @RestClient
    private SimpleGetApi configClient;

    @Inject
    @RestClient
    private MyClientWithAnnotations annotationClient;

    @Deployment
    public static WebArchive createDeployment() {
        String uriProp = SimpleGetApi.class.getName() + "/mp-rest/uri=" + STUB_URI;
        String providerProp = SimpleGetApi.class.getName() + "/mp-rest/providers=" + MyFilter.class.getName();

        String simpleName = CDIManagedProviderTest.class.getSimpleName();
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, simpleName + ".jar")
                .addClasses(SimpleGetApi.class)
                .addAsManifestResource(new StringAsset(String.format(uriProp + "%n" + providerProp)),
                        "microprofile-config.properties")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war").addAsLibrary(jar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testCDIProviderSpecifiedInMPConfig() throws Exception {
        Response r = configClient.executeGet();
        assertEquals(r.getStatus(), 200);
    }

    @Test
    public void testCDIProviderSpecifiedViaAnnotation() throws Exception {
        Response r = annotationClient.executeGet();
        assertEquals(r.getStatus(), 200);
    }

    @Test
    public void testCDIProviderSpecifiedViaRestClientBuilder() throws Exception {
        MyProgrammaticClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create(STUB_URI))
                .register(MyFilter.class)
                .build(MyProgrammaticClient.class);
        Response r = client.executeGet();
        assertEquals(r.getStatus(), 200);
    }

    @Test
    public void testInstanceProviderSpecifiedViaRestClientBuilderDoesNotUseCDIManagedProvider() throws Exception {
        MyProgrammaticClient client = RestClientBuilder.newBuilder()
                .baseUri(URI.create("http://localhost:9080/stub"))
                .register(new MyFilter())
                .build(MyProgrammaticClient.class);
        Response r = client.executeGet();
        assertEquals(r.getStatus(), 204);
    }

    @ApplicationScoped
    public static class MyFilter implements ClientRequestFilter {

        protected boolean postConstructInvoked;

        @Inject
        protected BeanManager beanManager;

        @PostConstruct
        public void postConstruct() {
            postConstructInvoked = true;
        }

        @Override
        public void filter(ClientRequestContext requestContext) throws IOException {
            requestContext.abortWith(Response.status(beanManager != null && postConstructInvoked ? 200 : 204).build());
        }
    }

    @RegisterRestClient(baseUri = STUB_URI)
    @RegisterProvider(MyFilter.class)
    public static interface MyClientWithAnnotations {
        @GET
        Response executeGet();
    }

    public static interface MyProgrammaticClient {
        @GET
        Response executeGet();
    }
}
