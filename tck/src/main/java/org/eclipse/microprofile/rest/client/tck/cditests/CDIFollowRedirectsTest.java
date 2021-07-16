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

import static org.eclipse.microprofile.rest.client.tck.FollowRedirectsTest.testDefault;
import static org.eclipse.microprofile.rest.client.tck.FollowRedirectsTest.testFollows;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.FollowRedirectsTest;
import org.eclipse.microprofile.rest.client.tck.WiremockArquillianTest;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApiWithConfigKey;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.github.tomakehurst.wiremock.client.WireMock;

import jakarta.inject.Inject;

/**
 * Verifies auto following redirects is performed when configured via MP Confg and CDI.
 */
public class CDIFollowRedirectsTest extends WiremockArquillianTest {
    @Inject
    @RestClient
    private SimpleGetApi defaultClient;

    @Inject
    @RestClient
    private SimpleGetApiWithConfigKey redirectingClient;

    @Deployment
    public static WebArchive createDeployment() {
        String urlProperty1 = SimpleGetApi.class.getName() + "/mp-rest/uri=" + getStringURL();
        String urlProperty2 = "myConfigKey/mp-rest/uri=" + getStringURL();
        String redirectProperty = "myConfigKey/mp-rest/followRedirects=true";
        String simpleName = CDIFollowRedirectsTest.class.getSimpleName();
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, simpleName + ".jar")
                .addClasses(SimpleGetApi.class, SimpleGetApiWithConfigKey.class,
                        FollowRedirectsTest.class, WiremockArquillianTest.class)
                .addAsManifestResource(
                        new StringAsset(String.format(redirectProperty + "%n" + urlProperty1 + "%n" + urlProperty2)),
                        "microprofile-config.properties")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
                .addAsLibrary(jar)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @BeforeMethod
    public void reset() {
        WireMock.reset();
    }

    @Test
    public void test301Default() throws Exception {
        testDefault(301, defaultClient);
    }

    @Test
    public void test301Follows() throws Exception {
        testFollows(301, redirectingClient);
    }

    @Test
    public void test302Default() throws Exception {
        testDefault(302, defaultClient);
    }

    @Test
    public void test302Follows() throws Exception {
        testFollows(302, redirectingClient);
    }

    @Test
    public void test303Default() throws Exception {
        testDefault(303, defaultClient);
    }

    @Test
    public void test303Follows() throws Exception {
        testFollows(303, redirectingClient);
    }

    @Test
    public void test307Default() throws Exception {
        testDefault(307, defaultClient);
    }

    @Test
    public void test307Follows() throws Exception {
        testFollows(307, redirectingClient);
    }
}
