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

import org.eclipse.microprofile.rest.client.ext.AsyncInvocationInterceptor;

public class TLAsyncInvocationInterceptor implements AsyncInvocationInterceptor {

    private final TLAsyncInvocationInterceptorFactory factory;
    private final Integer tlValue;

    TLAsyncInvocationInterceptor(TLAsyncInvocationInterceptorFactory factory, Integer tlValue) {
        this.factory = factory;
        this.tlValue = tlValue;
    }

    @Override
    public void prepareContext() {
        factory.getData().put("preThreadId", Thread.currentThread().getId());
    }

    @Override
    public void applyContext() {
        factory.getData().put("postThreadId", Thread.currentThread().getId());
        if (TLAsyncInvocationInterceptorFactory.getTlInt() != 0) {
            System.out.println("Using recycled thread - with non-default ThreadLocal settings.");
            TLAsyncInvocationInterceptorFactory.setTlInt(-1);
            return;
        }
        TLAsyncInvocationInterceptorFactory.setTlInt(tlValue);
    }

    @Override
    public void removeContext() {
        factory.getData().put("removeThreadId", Thread.currentThread().getId());
        factory.getData().put("AsyncThreadLocalPre", TLAsyncInvocationInterceptorFactory.getTlInt());
        TLAsyncInvocationInterceptorFactory.setTlInt(0);
        factory.getData().put("AsyncThreadLocalPost", TLAsyncInvocationInterceptorFactory.getTlInt());
    }
}
