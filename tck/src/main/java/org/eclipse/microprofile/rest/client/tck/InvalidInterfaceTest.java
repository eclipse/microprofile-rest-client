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

import java.io.IOException;
import java.net.URL;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.eclipse.microprofile.rest.client.tck.interfaces.MissingPathParam;
import org.eclipse.microprofile.rest.client.tck.interfaces.MissingPathParamSub;
import org.eclipse.microprofile.rest.client.tck.interfaces.MissingUriTemplate;
import org.eclipse.microprofile.rest.client.tck.interfaces.MultipleHTTPMethodAnnotations;
import org.eclipse.microprofile.rest.client.tck.interfaces.TemplateMismatch;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.testng.annotations.Test;

public class InvalidInterfaceTest extends Arquillian{
    @Deployment
    public static Archive<?> createDeployment() {
        return ShrinkWrap.create(WebArchive.class, InvalidInterfaceTest.class.getSimpleName()+".war");
    }

    @Test(expectedExceptions={RestClientDefinitionException.class})
    public void testExceptionThrownWhenInterfaceHasMethodWithMultipleHTTPMethodAnnotations() throws IOException {
        RestClientBuilder.newBuilder().baseUrl(new URL("http://localhost:8080/")).build(MultipleHTTPMethodAnnotations.class);
    }

    @Test(expectedExceptions={RestClientDefinitionException.class})
    public void testExceptionThrownWhenInterfaceHasMethodWithMissingPathParamAnnotation_templateDeclaredAtTypeLevel() throws IOException {
        RestClientBuilder.newBuilder().baseUrl(new URL("http://localhost:8080/")).build(MissingPathParam.class);
    }

    @Test(expectedExceptions={RestClientDefinitionException.class})
    public void testExceptionThrownWhenInterfaceHasMethodWithMissingPathParamAnnotation_templateDeclaredAtMethodLevel() throws IOException {
        RestClientBuilder.newBuilder().baseUrl(new URL("http://localhost:8080/")).build(MissingPathParamSub.class);
    }

    @Test(expectedExceptions={RestClientDefinitionException.class})
    public void testExceptionThrownWhenInterfaceHasMethodWithPathParamAnnotationButNoURITemplate() throws IOException {
        RestClientBuilder.newBuilder().baseUrl(new URL("http://localhost:8080/")).build(MissingUriTemplate.class);
    }

    @Test(expectedExceptions={RestClientDefinitionException.class})
    public void testExceptionThrownWhenInterfaceHasMethodWithMismatchedPathParameter() throws IOException {
        RestClientBuilder.newBuilder().baseUrl(new URL("http://localhost:8080/")).build(TemplateMismatch.class);
    }
}
