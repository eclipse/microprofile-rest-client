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
import org.eclipse.microprofile.rest.client.tck.interfaces.ConfigKeyClient2;
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
 * Verifies that users can simplify config properties with a config key, and that the fully-qualified classname-based
 * property takes precedence over config keys.
 */
public class ConfigKeyTest extends Arquillian {

    @Inject
    @RestClient
    private ConfigKeyClient client1;

    @Inject
    @RestClient
    private ConfigKeyClient2 client2;

    @Deployment
    public static WebArchive createDeployment() {
        String uriPropertyName = "myConfigKey/mp-rest/uri";
        String uriValue = "http://localhost:1234/configKeyUri";
        String overridePropName = ConfigKeyClient2.class.getName() + "/mp-rest/uri";
        String overridePropValue = "http://localhost:5678/FQCNUri";
        String simpleName = ConfigKeyTest.class.getSimpleName();
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, simpleName + ".jar")
                .addClasses(ConfigKeyClient.class,
                        ConfigKeyClient2.class,
                        ReturnWithURLRequestFilter.class)
                .addAsManifestResource(new StringAsset(
                        String.format(uriPropertyName + "=" + uriValue + "%n" +
                                overridePropName + "=" + overridePropValue)),
                        "microprofile-config.properties")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
                .addAsLibrary(jar)
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testConfigKeyUsedForUri() throws Exception {
        assertEquals(client1.get(), "GET http://localhost:1234/configKeyUri/hello");
    }
    @Test
    public void testFullyQualifiedClassnamePropTakesPrecedenceOverConfigKey() throws Exception {
        assertEquals(client2.get2(), "GET http://localhost:5678/FQCNUri/hello2");
    }

}
