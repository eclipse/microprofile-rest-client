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

package org.eclipse.microprofile.rest.client.tck.providers;

public class Widget {

    private String name;
    private int length;

    public static Widget fromString(String s) {
        String[] split = s.split(":");
        return new Widget(split[0], Integer.parseInt(split[1]));
    }

    public Widget(String name, int length) {
        this.name = name;
        this.length = length;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return name + ":" + length;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Widget) && o != null && ((Widget) o).name.equals(name) && ((Widget) o).length == length;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
