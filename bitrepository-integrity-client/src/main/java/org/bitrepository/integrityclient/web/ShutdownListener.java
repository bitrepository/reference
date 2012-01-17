package org.bitrepository.integrityclient.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Listener has two intentions
 * 1) Acquire necessary information at startup to locate configuration files and create the first instance 
 * 		of the basic client, so everything is setup before the first users start using the webservice. 
 * 2) In time shut the service down in a proper manner, so no threads will be orphaned.   
 */
public class ShutdownListener implements ServletContextListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Do initialization work  
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
    	// Method that's run when the war file is deployed. 
    	// Can be used to init which ever might be interesting
        log.debug("Servlet context initialized");
    }

    /**
     * Do teardown work. 
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Method that's run when the war file is undeployed. 
    	// Can be used to shut everything down nicely..
    	log.debug("Servlet context destroyed");
    }

}
