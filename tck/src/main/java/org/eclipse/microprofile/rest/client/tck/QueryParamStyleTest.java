/*
 * Copyright 2020 Contributors to the Eclipse Foundation
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

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.net.URI;
import java.util.Arrays;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.ext.QueryParamStyle;
import org.eclipse.microprofile.rest.client.tck.interfaces.StringClient;
import org.eclipse.microprofile.rest.client.tck.providers.ReturnWithURLRequestFilter;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

public class QueryParamStyleTest extends Arquillian {
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, InheritanceTest.class.getSimpleName() + ".war")
                .addClasses(ReturnWithURLRequestFilter.class, StringClient.class);
    }

    @Test
    public void defaultStyleIsMultiPair() throws Exception {
        String expected = "?myParam=foo&myParam=bar&myParam=baz";
        StringClient client = builder().build(StringClient.class);
        test(client, expected);
    }

    @Test
    public void explicitMultiPair() throws Exception {
        String expected = "?myParam=foo&myParam=bar&myParam=baz";
        StringClient client = builder().queryParamStyle(QueryParamStyle.MULTI_PAIRS)
                .build(StringClient.class);
        test(client, expected);
    }

    @Test
    public void commaSeparated() throws Exception {
        String expected = "?myParam=foo,bar,baz";
        StringClient client = builder().queryParamStyle(QueryParamStyle.COMMA_SEPARATED)
                .build(StringClient.class);
        test(client, expected);
    }

    @Test
    public void arrayPairs() throws Exception {
        String expected = "?myParam[]=foo&myParam[]=bar&myParam[]=baz";
        StringClient client = builder().queryParamStyle(QueryParamStyle.ARRAY_PAIRS)
                .build(StringClient.class);
        test(client, expected);
    }

    public static void test(StringClient client, String expected) {
        String responseStr = client.multiValues(Arrays.asList("foo", "bar", "baz"));
        assertNotNull(responseStr, "Response entity is null");
        assertTrue(responseStr.contains(expected),
                "Expected snippet, " + expected + ", in: " + responseStr);
    }

    private RestClientBuilder builder() {
        return RestClientBuilder.newBuilder()
                .register(new ReturnWithURLRequestFilter())
                .baseUri(URI.create("http://localhost/stub"));
    }
}
