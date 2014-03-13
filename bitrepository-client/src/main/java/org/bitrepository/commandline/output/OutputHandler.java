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

/**
 * The interface for handling the output from the commandline client.
 */
public interface OutputHandler {
    /**
     * Handles the debug information for the commandline client.
     * @param debug The debug information to be handled.
     */
    void debug(String debug);
    
    /**
     * Handles the startup information for the commandline client.  
     * @param info The information to be handled.
     */
    void startupInfo(String info);
    
    /**
     * Handles the complete events.
     * @param msg The message regarding the complete event.
     * @param event The final event to handle.
     */
    void completeEvent(String msg, OperationEvent event);
    
    /**
     * Handles a warning for the commandline client.
     * @param warning The warning to be handled.
     */
    void warn(String warning);
    
    /** 
     * Handles an error.
     * @param error The message for the error.
     */
    void error(String error);

    /**
     * Handles an error.
     * @param error The message for the error.
     * @param e The throwable for the error.
     */
    void error(String error, Throwable e);
    
    /**
     * Handles the outputting of a line of results 
     * @param line, the line to output
     */
    void resultLine(String line);
    
    /**
     * Handles the outputting of the header (or ledgend) of the following lines
     * @param header, the header to output 
     */
    void resultHeader(String header);
    
    /**
     * To set or unset if the output handler should behave verbosely or not. 
     */
    void setVerbosity(boolean verbose);
}
