/*
 * #%L
 * Bitrepository Audit Trail Service
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.audittrails.webservice;

import org.bitrepository.audittrails.service.AuditTrailServiceFactory;
import org.bitrepository.protocol.service.AbstractBitrepositoryContextListener;
import org.bitrepository.protocol.service.BitrepositoryService;


/**
 * The Listener has two intentions
 * 1) Acquire necessary information at startup to locate configuration files and create the first instance 
 * 		of the basic client, so everything is setup before the first users start using the webservice. 
 * 2) In time shut the service down in a proper manner, so no threads will be orphaned.   
 */
public class AuditTrailServiceContextListener extends AbstractBitrepositoryContextListener {
       
    @Override
    public BitrepositoryService getService() {
        return AuditTrailServiceFactory.getAuditTrailService();
    }

    @Override
    public void setConfigurationDirectory(String configutrationDir) {
        AuditTrailServiceFactory.init(configutrationDir);        
    }

    @Override
    public String getSettingsParameter() {
        return "auditTrailServiceConfDir";
    }
    
    
    
    /**
     * Do initialization work  
     */
    /*@Override
    public void contextInitialized(ServletContextEvent sce) {
        String confDir = sce.getServletContext().getInitParameter();
        if(confDir == null) {
            throw new RuntimeException("No configuration directory specified!");
        }
        log.debug("Configuration dir = " + confDir);
        //System.setProperty(ConfigurationFactory.CONFIGURATION_DIR_SYSTEM_PROPERTY, confDir);
        try {
            new LogbackConfigLoader(confDir + "/logback.xml");
        } catch (Exception e) {
        	log.info("Failed to read log configuration file. Falling back to default.");
        } 
        AuditTrailServiceFactory.init(confDir);
        AuditTrailService service = AuditTrailServiceFactory.getAuditTrailService();
        log.debug("Servlet context initialized");
    }*/
    
    /**
     * Do teardown work. 
     */
   /* @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Method that's run when the war file is undeployed. 
        // Can be used to shut everything down nicely..
        log.debug("Servlet context destroyed");
    }*/
    
}
