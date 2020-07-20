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
 * APIs to aid in CDI-based injection of MP Rest Client implementations.  These
 * annotations are used both to mark an interface as registered Rest Client and
 * also to designate that an implementation of that interface should be injected
 * at a specific injection point.
 *
 * Example:
 * <pre>
 * &#064;RegisterProvider
 * &#064;Dependent
 * public interface MyClientService {
 *     &#064;GET
 *     &#064;Path("/myService/{id}")
 *     Widget getWidget(&#064;PathParam("id") String id);
 * }
 *
 * ...
 * &#064;ApplicationScoped
 * public class MyBean {
 *     &#064;Inject
 *     &#064;RestClient
 *     MyClientService service;
 *     ...
 * }
 * </pre>
 */
@org.osgi.annotation.versioning.Version("1.2.1")
@org.osgi.annotation.versioning.ProviderType
package org.eclipse.microprofile.rest.client.inject;
