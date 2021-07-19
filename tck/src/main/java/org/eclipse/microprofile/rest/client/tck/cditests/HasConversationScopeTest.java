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

import static org.testng.Assert.assertEquals;

import java.util.Set;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.ConfigKeyClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

/**
 * This test verifies that you can configure a bean of ConversationScoped, as well as have the scope on an interface.
 */
public class HasConversationScopeTest extends Arquillian {
    @Inject
    private BeanManager beanManager;

    @Deployment
    public static WebArchive createDeployment() {
        String url = SimpleGetApi.class.getName() + "/mp-rest/url=http://localhost:8080";
        String url2 = MyConversationScopedApi.class.getName() + "/mp-rest/url=http://localhost:8080";
        String scope = SimpleGetApi.class.getName() + "/mp-rest/scope=" + ConversationScoped.class.getName();
        String configKeyScope = "myConfigKey/mp-rest/scope=" + ConversationScoped.class.getName();
        String simpleName = HasConversationScopeTest.class.getSimpleName();
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, simpleName + ".jar")
                .addClasses(SimpleGetApi.class, MyConversationScopedApi.class, ConfigKeyClient.class)
                .addAsManifestResource(new StringAsset(url + "\n" + scope + "\n" + url2 + "\n" + configKeyScope),
                        "microprofile-config.properties")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
                .addAsLibrary(jar)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testHasConversationScoped() {
        Set<Bean<?>> beans = beanManager.getBeans(SimpleGetApi.class, RestClient.LITERAL);
        Bean<?> resolved = beanManager.resolve(beans);
        assertEquals(resolved.getScope(), ConversationScoped.class);
    }

    @Test
    public void testHasConversationScopedFromConfigKey() {
        Set<Bean<?>> beans = beanManager.getBeans(ConfigKeyClient.class, RestClient.LITERAL);
        Bean<?> resolved = beanManager.resolve(beans);
        assertEquals(resolved.getScope(), ConversationScoped.class);
    }

    @Test
    public void testHasConversationScopedWhenAnnotated() {
        Set<Bean<?>> beans = beanManager.getBeans(MyConversationScopedApi.class, RestClient.LITERAL);
        Bean<?> resolved = beanManager.resolve(beans);
        assertEquals(resolved.getScope(), ConversationScoped.class);
    }

    @Path("/")
    @ConversationScoped
    @RegisterRestClient
    public interface MyConversationScopedApi {
        @GET
        public Response get();
    }
}
