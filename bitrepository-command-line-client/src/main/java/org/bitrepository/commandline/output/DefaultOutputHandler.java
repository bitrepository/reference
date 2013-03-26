/*
 * #%L
 * Bitrepository Command Line
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
package org.bitrepository.commandline.output;

import org.bitrepository.client.eventhandler.OperationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default handler of the output from the Commandline Client.
 */
public class DefaultOutputHandler implements OutputHandler {
    /** The log. */
    private final Logger log;

    /**
     * Constructor.
     * @param c The class for whom to handle the output.
     */
    @SuppressWarnings("rawtypes")
    public DefaultOutputHandler(Class c) {
        log = LoggerFactory.getLogger(c);
    }
    
    @Override
    public void debug(String debug) {
        log.debug(debug);
    }

    @Override
    public void startupInfo(String s) {
        System.err.println(s);
    }

    @Override
    public void warn(String warning) {
        System.err.println(warning);
        log.warn(warning);
    }
    
    @Override
    public void error(String error, Throwable e) {
        System.err.println(error);
        e.printStackTrace(System.err);
        log.error(error, e);
    }

    @Override
    public void completeEvent(String msg, OperationEvent event) {
        System.err.println("Final Result: " + event.getEventType());
        log.info(msg);
        log.debug(event.toString());
    }

    @Override
    public void resultLine(String line) {
        System.out.println(line);
    }

    @Override
    public void resultHeader(String header) {
        System.out.println(header);
    }
}
