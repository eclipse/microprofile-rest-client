/*
 * Copyright 2018, 2021 Contributors to the Eclipse Foundation
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

import java.lang.reflect.Method;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@Loggable
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class LoggableInterceptor {

    private static String invocationMethod;
    private static Class<?> invocationClass;
    private static Object result;

    public static String getInvocationMethod() {
        return invocationMethod;
    }

    public static Class<?> getInvocationClass() {
        return invocationClass;
    }

    public static Object getResult() {
        return result;
    }

    public static void reset() {
        invocationClass = null;
        invocationMethod = null;
        result = null;
    }

    @AroundInvoke
    public Object logInvocation(InvocationContext ctx) throws Exception {
        Method m = ctx.getMethod();
        invocationClass = m.getDeclaringClass();
        invocationMethod = m.getName();

        Object returnVal = ctx.proceed();
        result = returnVal;
        return returnVal;
    }
}
