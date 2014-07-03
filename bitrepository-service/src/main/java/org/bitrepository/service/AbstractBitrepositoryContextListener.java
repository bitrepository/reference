/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.service;

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
    /** The log.*/
    private final Logger log = LoggerFactory.getLogger(getClass());
        
    /**
     * Return the path to the service's configuration directory 
     */
    public abstract String getSettingsParameter();
    
    /**
     * Method to get an instance of the service for the context
     */
    public abstract LifeCycledService getService();
    
    /**
     * Sets up the configuration 
     */
    public abstract void initialize(String configurationDir);
    
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
        try {
            initialize(confDir);
            getService();
        } catch (RuntimeException e) {
            // This is to ensure the message of what went wrong will go into the service's own logfile
            // rather than just in Tomcat's localhost logfile where it can be hard to find. 
            // Rethrowing the exception makes the service shutdown again. 
            log.error("Caught runtime exception:", e);
            throw e;
        }
        log.debug("Servlet context initialized");
        
    }
    
    /**
     * Method called at servlet shutdown. 
     */
    public void contextDestroyed(ServletContextEvent sce) {
        getService().shutdown();
        log.debug("Servlet context destroyed");
    }
}
