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

