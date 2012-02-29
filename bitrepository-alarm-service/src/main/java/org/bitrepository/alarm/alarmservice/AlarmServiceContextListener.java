/*
 * #%L
 * Bitrepository Alarm Service
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
package org.bitrepository.alarm.alarmservice;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.bitrepository.alarm.AlarmStore;
import org.bitrepository.alarm.AlarmStoreFactory;
import org.bitrepository.alarm.utils.LogbackConfigLoader;
import org.bitrepository.common.ConfigurationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Listener has two intentions
 * 1) Acquire necessary information at startup to locate configuration files and create the first instance 
 * 		of the basic client, so everything is setup before the first users start using the webservice. 
 * 2) In time shut the service down in a proper manner, so no threads will be orphaned.   
 */
public class AlarmServiceContextListener implements ServletContextListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Performs work required to bring up the alarm service. 
     * This includes setting up locations of configuration directories and initializing the AlarmStore 
     * so it is connected to the messagebus and ready. 
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String confDir = sce.getServletContext().getInitParameter("alarmServiceConfDir");
        if(confDir == null) {
        	throw new RuntimeException("No configuration directory specified!");
        }
        log.debug("Configuration dir = " + confDir);
        System.setProperty(ConfigurationFactory.CONFIGURATION_DIR_SYSTEM_PROPERTY, confDir);
        try {
			new LogbackConfigLoader(confDir + "/logback.xml");
		} catch (Exception e) {
			log.info("Failed to read log configuration file. Falling back to default.");
		} 
        AlarmStoreFactory.init(confDir);
        AlarmStore alarmStore = AlarmStoreFactory.getAlarmStore();
        log.debug("Servlet context initialized");
    }

    /**
     * Does the work of shutting the alarm service down gracefully. 
     * This is done by calling the Alarm Store's shutdown method. 
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        AlarmStore alarmStore = AlarmStoreFactory.getAlarmStore();
        alarmStore.shutdown(); 
        log.debug("Servlet context destroyed");
    }

}