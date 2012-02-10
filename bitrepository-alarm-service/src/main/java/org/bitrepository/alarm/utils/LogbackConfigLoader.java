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
package org.bitrepository.alarm.utils;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class LogbackConfigLoader {
	private Logger log = LoggerFactory.getLogger(LogbackConfigLoader.class);
	
	public LogbackConfigLoader(String configFileLocation) throws IOException, JoranException{
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		File configFile = new File(configFileLocation);
		if(!configFile.exists()){
			throw new IOException("Logback External Config File Parameter does not reference a file that exists");
		}
		
		if(!configFile.isFile()){
			throw new IOException("Logback External Config File Parameter exists, but does not reference a file");
		}
		
		if(!configFile.canRead()){
			throw new IOException("Logback External Config File exists and is a file, but cannot be read.");
		}
		
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(loggerContext);
		loggerContext.reset();
		configurator.doConfigure(configFileLocation);
		log.info("Configured Logback with config file from: " + configFileLocation);
		
	}
}

