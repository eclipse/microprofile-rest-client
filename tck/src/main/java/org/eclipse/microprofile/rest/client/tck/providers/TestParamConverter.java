/*
 * Copyright 2017, 2021 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.tck.providers;

import jakarta.ws.rs.ext.ParamConverter;

public class TestParamConverter implements ParamConverter<Widget> {
    @Override
    public Widget fromString(String s) {
        return s == null ? null : Widget.fromString(s);
    }

    @Override
    public String toString(Widget w) {
        return w == null ? null : w.toString();
    }
}
