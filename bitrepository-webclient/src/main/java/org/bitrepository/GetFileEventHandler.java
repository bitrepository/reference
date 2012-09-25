/*
 * #%L
 * Bitrepository Webclient
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
package org.bitrepository;

import java.net.URL;
import java.util.List;

import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;

/**
 * Event handler for the asynchronous GetFileIDs method.   
 */
public class GetFileEventHandler implements EventHandler {

    private EventHandler logger;
    private List<URL> completedFiles;
    private URL url;

    public GetFileEventHandler(URL url, List<URL> completedFiles, EventHandler logger) {
        this.logger = logger;
        this.completedFiles = completedFiles;
        this.url = url;
    }

    @Override
    public void handleEvent(OperationEvent event) {
        logger.handleEvent(event);
        switch(event.getEventType()) {
        case IDENTIFY_REQUEST_SENT:
            break;
        case COMPONENT_IDENTIFIED:
            break;
        case IDENTIFICATION_COMPLETE:
            break;
        case REQUEST_SENT:
            break;
        case PROGRESS:
            break;
        case COMPONENT_COMPLETE:
            break;
        case COMPLETE:
            completedFiles.add(url);
            break;
        case COMPONENT_FAILED:
            break;
        case FAILED:
            break;
        case IDENTIFY_TIMEOUT: 
            break;
        case WARNING:
            break;
        }
    }
}
