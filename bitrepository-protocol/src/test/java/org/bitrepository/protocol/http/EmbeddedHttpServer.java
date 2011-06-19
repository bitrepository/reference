package org.bitrepository.protocol.http;

import java.io.File;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

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
}
