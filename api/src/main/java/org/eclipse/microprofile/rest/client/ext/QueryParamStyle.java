/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.ext;

/**
 * A QueryParamStyle enum is used to specify how multiple values are handled
 * when constructing the query portion of the URI. For example, a client
 * interface may take a collection of strings as it's query parameter. This enum
 * determines the style:
 * 
 * <pre>
 * public interface MultiParamClient {
 *     void sendMultipleQueryParams(&#064;QueryParam("foo") List&lt;String&gt; strings);
 * }
 * </pre>
 * 
 * The style selected when building this client instance will determine the format
 * of the query portion of the URI.
 *
 * @since 2.0
 */
public enum QueryParamStyle {

    /**
     * Multiple parameter instances, e.g.:
     * <code>foo=v1&amp;foot=v2&amp;foo=v3</code>
     * 
     * This is the default if no style is configured.
     */
    MULTI_PAIRS,

    /** A single parameter instance with multiple, comma-separated values, e.g.:
     * <code>foo=v1,v2,v3</code>
     */
    COMMA_SEPARATED,

    /**
     * Multiple parameter instances with square brackets for each parameter, e.g.:
     * <code>foo[]=v1&amp;foo[]=v2&amp;foo[]=v3</code>
     */
    ARRAY_PAIRS
}
