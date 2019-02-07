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

package org.eclipse.microprofile.rest.client.tck.interfaces;

import java.lang.reflect.Method;

import javax.annotation.Priority;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Loggable
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class LoggableInterceptor {

    private static String invocationMessage;

    public static String getInvocationMessage() {
        return invocationMessage;
    }

    public static void setInvocationMessage(String msg) {
        invocationMessage = msg;
    }

    @AroundInvoke
    public Object logInvocation(InvocationContext ctx) throws Exception {
        Method m = ctx.getMethod();
        invocationMessage = m.getDeclaringClass().getName() + "." + m.getName();

        Object returnVal = ctx.proceed();
        invocationMessage += " " + returnVal;
        return returnVal;
    }
}
