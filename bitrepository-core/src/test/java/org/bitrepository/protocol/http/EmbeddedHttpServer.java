/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.protocol.http;

import java.io.File;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

/**
 * May be use to programmatically start an embedded http server, replacing the need for an already running server. 
 * 
 * The implementations used an embedded Jetty server.
 * 
 * Note that this doesn't currently work, because of lack of put support. It should be possible to exten the Jetty 
 * server with this functionality
 */
public class EmbeddedHttpServer {
    public static final String PROTOCOL = "http";
    public static final int PORT_NUMBER = 16789;
    public static final String HTTP_SERVER_NAME = "localhost";
    public static final String HTTP_SERVER_PATH = "/dav/";

    private final Server server = new Server();

    public EmbeddedHttpServer() {
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(PORT_NUMBER);
        server.addConnector(connector);

        ResourceHandler resource_handler = new ResourceHandler();
        resource_handler.setDirectoriesListed(true);
        resource_handler.setWelcomeFiles(new String[]{ "index.html" });

        File httpServerDir = new File("target/httpserver/dav/");
        httpServerDir.mkdirs();

        resource_handler.setResourceBase(httpServerDir.getPath());

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
        server.setHandler(handlers);
    }

    public void start() throws Exception {
        server.start();
        //server.join();
    }

    public void stop() throws Exception {
        server.stop();
    }
}
