/*
 *******************************************************************************
 * Copyright (c) 2016-2020 Contributors to the Eclipse Foundation
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
 * APIs for building a type-safe RESTful client leveraging existing JAX-RS
 * APIs, for example:
 * <pre>
 * public interface MyClientService {
 *     &#064;GET
 *     &#064;Path("/myService/{id}")
 *     Widget getWidget(&#064;PathParam("id") String id);
 * }
 *
 * ...
 *
 * MyClientService service = RestClientBuilder.newBuilder()
 *                                            .baseUrl(url)
 *                                            .build();
 * Widget w = service.getWidget(widgetId); // invokes remote service, returns domain object
 * </pre>
 * @since 1.0
 */
@org.osgi.annotation.versioning.Version("1.4")
@org.osgi.annotation.versioning.ProviderType
package org.eclipse.microprofile.rest.client;
