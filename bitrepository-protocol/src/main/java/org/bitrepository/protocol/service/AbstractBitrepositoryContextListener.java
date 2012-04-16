package org.bitrepository.protocol.service;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.bitrepository.protocol.utils.LogbackConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Listener has two intentions
 * 1) Acquire necessary information at startup to locate configuration files and create the first instance 
 *      of the basic client, so everything is setup before the first users start using the webservice. 
 * 2) In time shut the service down in a proper manner, so no threads will be orphaned.   
 */
public abstract class AbstractBitrepositoryContextListener implements ServletContextListener {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private BitrepositoryService service;
    
    /**
     * Return the path to the service's configuration directory 
     */
    public abstract String getSettingsParameter();
    
    /**
     * Method to get an instance of the service for the context
     */
    public abstract BitrepositoryService getService();
    
    /**
     * Sets up the configuration 
     */
    public abstract void initialize(String configutrationDir);
    
    /**
     * Method called at servlet initialization 
     */
    public void contextInitialized(ServletContextEvent sce) {
        String confDir = sce.getServletContext().getInitParameter(getSettingsParameter());
        if(confDir == null) {
            throw new RuntimeException("No configuration directory specified!");
        }
        log.debug("Configuration dir = " + confDir);
        
        try {
            new LogbackConfigLoader(confDir + "/logback.xml");
        } catch (Exception e) {
            log.info("Failed to read log configuration file. Falling back to default.");
        } 
        initialize(confDir);
        service = getService();
        log.debug("Servlet context initialized");
        
    }
    
    /**
     * Method called at servlet shutdown. 
     */
    public void contextDestroyed(ServletContextEvent sce) {
        if(service != null) {
            service.shutdown();
        }
        log.debug("Servlet context destroyed");
    }
    
}
