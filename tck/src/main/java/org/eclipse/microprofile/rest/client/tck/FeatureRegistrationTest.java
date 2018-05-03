/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.microprofile.rest.client.tck;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.tck.interfaces.FeatureProviderClient;
import org.eclipse.microprofile.rest.client.tck.interfaces.SimpleGetApi;
import org.eclipse.microprofile.rest.client.tck.providers.InjectedSimpleFeature;
import org.eclipse.microprofile.rest.client.tck.providers.SimpleFeature;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

import javax.inject.Inject;
import static org.eclipse.microprofile.rest.client.tck.WiremockArquillianTest.getStringURL;

import static org.testng.Assert.assertTrue;

/**
 *
 */
public class FeatureRegistrationTest extends WiremockArquillianTest{
    @Deployment
    public static WebArchive createDeployment() {
        StringAsset mpConfig = new StringAsset(FeatureProviderClient.class.getName() + "/mp-rest/url=" + getStringURL());
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                .addClasses(SimpleFeature.class,
                        InjectedSimpleFeature.class,
                        SimpleGetApi.class,
                        FeatureProviderClient.class,
                        WiremockArquillianTest.class)
                .addAsManifestResource(mpConfig, "microprofile-config.properties")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return ShrinkWrap.create(WebArchive.class, FeatureRegistrationTest.class.getSimpleName() + ".war")
                .addAsLibrary(jar);
    }

    @Inject
    private FeatureProviderClient featureProviderClient;

    @Test
    public void testFeatureRegistrationViaBuilder() {
        SimpleFeature.reset();
        RestClientBuilder.newBuilder()
            .register(SimpleFeature.class)
            .baseUrl(getServerURL())
            .build(SimpleGetApi.class);

        assertTrue(SimpleFeature.wasInvoked(), "The SimpleFeature should have been invoked " +
            "when building the client");
    }

    @Test
    public void testFeatureRegistrationViaCDI() {
        assertTrue(InjectedSimpleFeature.wasInvoked(), "The InjectedSimpleFeature should have " +
            "been invoked when building the client");
    }
}
