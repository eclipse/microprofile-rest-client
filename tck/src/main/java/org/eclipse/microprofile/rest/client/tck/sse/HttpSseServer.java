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

import java.util.function.Consumer;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.testng.log4testng.Logger;

/**
 *
 * HTTP server which fires server sent events.
 */
public class HttpSseServer implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(HttpSseServer.class);

    private Server server;

    public HttpSseServer start(int port, Consumer<MyEventSource> consumer) {
        server = new Server(port);
        ServletHandler handler = new ServletHandler();
        ServletHolder holder = new ServletHolder(new MyEventSourceServlet(consumer));
        handler.addServletWithMapping(holder, "/*");
        server.setHandler(handler);

        try {
            server.start();
            LOG.debug("started");
        } catch (Exception e) {
            throw new RuntimeException("Failed to start SSE HTTP server", e);
        }
        return this;
    }

    public void stop() {
        try {
            server.stop();
            LOG.debug("stopped");
        } catch (Exception e) {
            LOG.error("Failed to stop", e);
            throw new RuntimeException("Failed to stop SSE HTTP server", e);
        }
    }

    @Override
    public void close() throws Exception {
        LOG.debug("close");
        stop();
    }
}
