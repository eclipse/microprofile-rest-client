/*
 *******************************************************************************
 * Copyright (c) 2018-2020 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

/**
 * APIs for extending MP Rest Client functionality - such as new providers.
 * Example:
 * <pre>
 * &#064;RegisterProvider(UnknownWidgetExceptionMapper.class)
 * public interface MyClientService {
 *     &#064;GET
 *     &#064;Path("/myService/{id}")
 *     Widget getWidget(&#064;PathParam("id") String id) throws UnknownWidgetException;
 * }
 * ...
 * public class UnknownWidgetExceptionMapper implements ResponseExceptionMapper {
 *
 *     &#064;Override
 *     UnknownWidgetException toThrowable(Response response) {
 *         String msg = "Could not find widget with ID of " + response.getHeaderString("WidgetId");
 *         return new UnknownWidgetException(msg)
 *     }
 * }
 * </pre>
 */
@org.osgi.annotation.versioning.Version("1.2")
@org.osgi.annotation.versioning.ProviderType
package org.eclipse.microprofile.rest.client.ext;
