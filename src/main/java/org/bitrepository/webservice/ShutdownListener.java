package org.bitrepository.webservice;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.bitrepository.BasicClient;
import org.bitrepository.BasicClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownListener implements ServletContextListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.debug("Servlet context initialized");
        BasicClient client = BasicClientFactory.getInstance();
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        BasicClient client = BasicClientFactory.getInstance();
        // TODO Ask client to gracefully shutdown 
        log.debug("Servlet context destroyed");
    }

}
