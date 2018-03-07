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

package org.eclipse.microprofile.rest.client.tck.providers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;
import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptorFactory;

public class TLAsyncInvocationInterceptorFactory implements AsyncInvocationInterceptorFactory {

    private static ThreadLocal<Integer> tlInt = ThreadLocal.withInitial( () -> { return new Integer(0); });

    private Map<String,Object> data = new ConcurrentHashMap<>();

    public static Integer getTlInt() {
        return tlInt.get();
    }

    static void setTlInt(Integer newTlInt) {
        tlInt.set(newTlInt);
    }

    public TLAsyncInvocationInterceptorFactory(Integer initialTlInt) {
        tlInt.set(initialTlInt);
    }

    public Map<String,Object> getData() {
        return data;
    }

    @Override
    public AsyncInvocationInterceptor newInterceptor() {
           return new TLAsyncInvocationInterceptor(this, getTlInt());
    }
}
