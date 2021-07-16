/*
 * Copyright 2019, 2021 Contributors to the Eclipse Foundation
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

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.ConfigKeyClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApiWithConfigKey;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithURLRequestFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import jakarta.inject.Inject;

/**
 * Verifies that users can simplify config properties with a single config key for multiple interfaces.
 */
public class ConfigKeyForMultipleInterfacesTest extends Arquillian {

    @Inject
    @RestClient
    private ConfigKeyClient client1;

    @Inject
    @RestClient
    private SimpleGetApiWithConfigKey client2;

    @Deployment
    public static WebArchive createDeployment() {
        String uriPropertyName = "myConfigKey/mp-rest/uri";
        String uriValue = "http://localhost:1234/configKeyUri";
        String simpleName = ConfigKeyTest.class.getSimpleName();
        String providerProperty = SimpleGetApiWithConfigKey.class.getName() +
                "/mp-rest/providers=" +
                ReturnWithURLRequestFilter.class.getName();
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, simpleName + ".jar")
                .addClasses(ConfigKeyClient.class,
                        SimpleGetApi.class,
                        SimpleGetApiWithConfigKey.class,
                        ReturnWithURLRequestFilter.class)
                .addAsManifestResource(new StringAsset(
                        String.format(uriPropertyName + "=" + uriValue + "%n" + providerProperty)),
                        "microprofile-config.properties")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
                .addAsLibrary(jar)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testConfigKeyUsedForUri() throws Exception {
        assertEquals(client1.get(), "GET http://localhost:1234/configKeyUri/hello");
        assertEquals(client2.executeGet().readEntity(String.class), "GET http://localhost:1234/configKeyUri");
    }

}
