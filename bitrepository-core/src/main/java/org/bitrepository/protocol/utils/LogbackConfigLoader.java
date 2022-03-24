/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.protocol.utils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Loads a Logback configurations.
 */
public class LogbackConfigLoader {
    private final Logger log = LoggerFactory.getLogger(LogbackConfigLoader.class);

    /**
     * Constructor.
     *
     * @param configFileLocation The path to the configuration file.
     * @throws JoranException If the configuration cannot be loaded.
     */
    public LogbackConfigLoader(String configFileLocation) throws JoranException {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        File configFile = new File(configFileLocation);
        if (!configFile.isFile()) {
            throw new IllegalArgumentException("Logback External Config File Parameter, "
                    + configFile.getAbsolutePath() + ", is not a file (either does not exists or is a directory).");
        }

        if (!configFile.canRead()) {
            throw new IllegalArgumentException("Logback External Config File cannot be read from '"
                    + configFile.getAbsolutePath() + "'");
        }

        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(loggerContext);
        loggerContext.reset();
        configurator.doConfigure(configFileLocation);
        log.info("Configured Logback with config file from: " + configFileLocation);
    }
}

