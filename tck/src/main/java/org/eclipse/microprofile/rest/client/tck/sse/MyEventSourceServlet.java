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
package org.eclipse.microprofile.rest.client.tck.sse;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.servlets.EventSource;
import org.eclipse.jetty.servlets.EventSourceServlet;

public class MyEventSourceServlet extends EventSourceServlet {
    private static final long serialVersionUID = -45238967561209543L;

    private final Consumer<MyEventSource> consumer;
    private final Set<MyEventSource> eventSources = new HashSet<>();

    MyEventSourceServlet(Consumer<MyEventSource> consumer) {
        this.consumer = consumer;
    }

    @Override
    protected EventSource newEventSource(HttpServletRequest request) {
        return new MyEventSource(consumer);
    }

    @Override
    public void destroy() {
        for (MyEventSource eventSource : eventSources) {
            eventSource.close();
        }
        super.destroy();
    }
}
