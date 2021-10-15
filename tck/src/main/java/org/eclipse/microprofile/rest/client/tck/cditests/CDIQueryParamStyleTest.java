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

import static org.eclipse.microprofile.rest.client.tck.QueryParamStyleTest.test;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.tck.QueryParamStyleTest;
import org.eclipse.microprofile.rest.client.tck.interfaces.StringClient;
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
 * Verifies the style used when sending multiple query param values configured via MP Confg and CDI.
 */
public class CDIQueryParamStyleTest extends Arquillian {
    @Inject
    @RestClient
    private DefaultStringClient defaultClient;

    @Inject
    @RestClient
    private MultiPairsStringClient multiPairsClient;

    @Inject
    @RestClient
    private CommaSeparatedStringClient commaSeparatedClient;

    @Inject
    @RestClient
    private ArrayPairsStringClient arrayPairsClient;

    @Deployment
    public static WebArchive createDeployment() {
        String urlProperty = "queryParamStyle/mp-rest/uri=http://localhost:8080/stub";
        String filterProperty = "queryParamStyle/mp-rest/providers=" + ReturnWithURLRequestFilter.class.getName();
        String multiPairsProperty = MultiPairsStringClient.class.getName()
                + "/mp-rest/queryParamStyle=MULTI_PAIRS";
        String commaSeparatedProperty = CommaSeparatedStringClient.class.getName()
                + "/mp-rest/queryParamStyle=COMMA_SEPARATED";
        String arrayPairsProperty = ArrayPairsStringClient.class.getName()
                + "/mp-rest/queryParamStyle=ARRAY_PAIRS";
        String simpleName = CDIQueryParamStyleTest.class.getSimpleName();
        JavaArchive jar = ShrinkWrap.create(JavaArchive.class, simpleName + ".jar")
                .addClasses(StringClient.class, ReturnWithURLRequestFilter.class, QueryParamStyleTest.class)
                .addAsManifestResource(new StringAsset(String.format(filterProperty + "%n"
                        + urlProperty + "%n"
                        + multiPairsProperty + "%n"
                        + commaSeparatedProperty + "%n"
                        + arrayPairsProperty + "%n")),
                        "microprofile-config.properties")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
        return ShrinkWrap.create(WebArchive.class, simpleName + ".war")
                .addAsLibrary(jar)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void defaultStyleIsMultiPair() throws Exception {
        String expected = "?myParam=foo&myParam=bar&myParam=baz";
        test(defaultClient, expected);
    }

    @Test
    public void explicitMultiPair() throws Exception {
        String expected = "?myParam=foo&myParam=bar&myParam=baz";
        test(multiPairsClient, expected);
    }

    @Test
    public void commaSeparated() throws Exception {
        String expected = "?myParam=foo,bar,baz";
        test(commaSeparatedClient, expected);
    }

    @Test
    public void arrayPairs() throws Exception {
        String expected = "?myParam[]=foo&myParam[]=bar&myParam[]=baz";
        test(arrayPairsClient, expected);
    }

    @RegisterRestClient(configKey = "queryParamStyle")
    public static interface DefaultStringClient extends StringClient {
    }

    @RegisterRestClient(configKey = "queryParamStyle")
    public static interface MultiPairsStringClient extends StringClient {
    }

    @RegisterRestClient(configKey = "queryParamStyle")
    public static interface CommaSeparatedStringClient extends StringClient {
    }

    @RegisterRestClient(configKey = "queryParamStyle")
    public static interface ArrayPairsStringClient extends StringClient {
    }

}
