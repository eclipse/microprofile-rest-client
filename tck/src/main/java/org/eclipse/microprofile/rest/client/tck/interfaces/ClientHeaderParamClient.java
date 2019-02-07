/*
 * Copyright 2018 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.tck.interfaces;

import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;

@ClientHeaderParam(name="InterfaceHeaderExplicit", value="interfaceExplicit")
@ClientHeaderParam(name="OverrideableExplicit", value="overrideableInterfaceExplicit")
@ClientHeaderParam(name="InterfaceHeaderComputed", value="{computeForInterface}")
@ClientHeaderParam(name="OverrideableComputed", value="{computeForInterface2}")
@ClientHeaderParam(name="OptionalInterfaceHeader", value="{fail}", required=false)
@ClientHeaderParam(name="InterfaceMultiValuedHeaderExplicit", value={"abc", "xyz"})
@Path("/")
public interface ClientHeaderParamClient {
    @GET
    String interfaceExplicit();

    @GET
    @ClientHeaderParam(name="MethodHeaderExplicit", value="methodExplicit")
    String methodExplicit();

    @GET
    String headerParamOverridesInterfaceExplicit(@HeaderParam("InterfaceHeaderExplicit") String param);

    @GET
    @ClientHeaderParam(name="MethodHeaderExplicit", value="methodExplicit")
    String headerParamOverridesMethodExplicit(@HeaderParam("MethodHeaderExplicit") String param);

    @GET
    @ClientHeaderParam(name="OverrideableExplicit", value="overriddenMethodExplicit")
    String methodClientHeaderParamOverridesInterfaceExplicit();

    @GET
    String interfaceComputed();

    @GET
    @ClientHeaderParam(name="MethodHeaderComputed", value="{computeForMethod}")
    String methodComputed();

    @GET
    String headerParamOverridesInterfaceComputed(@HeaderParam("InterfaceHeaderComputed") String param);

    @GET
    @ClientHeaderParam(name="MethodHeaderComputed", value="{computeForMethod2}")
    String headerParamOverridesMethodComputed(@HeaderParam("MethodHeaderComputed") String param);

    @GET
    @ClientHeaderParam(name="OverrideableComputed", value="{computeForMethod3}")
    String methodClientHeaderParamOverridesInterfaceComputed();

    @GET
    @ClientHeaderParam(name="OptionalMethodHeader", value="{fail}", required=false)
    @ClientHeaderParam(name="MethodHeaderExplicit", value="SomeValue")
    JsonObject methodOptionalMethodHeaderNotSentWhenComputeThrowsException();

    @GET
    @ClientHeaderParam(name="WillCauseFailure", value="{fail}")
    String methodRequiredComputeMethodFails();

    @GET
    @ClientHeaderParam(name="MultiValueInvokedFromAnotherClass",
                       value="{org.eclipse.microprofile.rest.client.tck.ext.HeaderGenerator.generateHeader}")
    String methodComputeMultiValuedHeaderFromOtherClass();

    default String computeForInterface() {
        return "interfaceComputed";
    }

    default String computeForInterface2(String headerName) {
        return "overrideableComputed";
    }

    default String computeForMethod(String headerName) {
        return headerName + "-X";
    }

    default String computeForMethod2(String headerName) {
        return headerName;
    }

    default String computeForMethod3() {
        return "overriddenMethodComputed";
    }

    default String fail() {
        throw new RuntimeException("intentional");
    }
}
