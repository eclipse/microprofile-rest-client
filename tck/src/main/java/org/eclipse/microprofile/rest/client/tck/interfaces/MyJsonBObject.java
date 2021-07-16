/*
 * Copyright (c) 2018, 2021 Contributors to the Eclipse Foundation
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

package org.eclipse.microprofile.rest.client.tck.interfaces;

import java.time.LocalDate;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.json.bind.annotation.JsonbTransient;

public class MyJsonBObject {

    @JsonbProperty("objectName")
    private String name;

    private int qty;

    @JsonbTransient
    private String ignoredField;

    private LocalDate date;

    public MyJsonBObject() {
        ignoredField = "CTOR";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonbProperty("quantity")
    public int getQty() {
        return qty;
    }

    @JsonbProperty("quantity")
    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getIgnoredField() {
        return ignoredField;
    }

    public void setIgnoredField(String value) {
        // not providing a setter is enough for field to be ignored.
        // To verify @JsonbTransient, a setter must be present.
        this.ignoredField = value;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
