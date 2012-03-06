package org.bitrepository;

import java.net.URL;
import java.util.List;

import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.eventhandler.OperationEvent;

/**
 *	Event handler for the asynchronous GetFileIDs method.   
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
	
	@SuppressWarnings("rawtypes")
    @Override
	public void handleEvent(OperationEvent event) {
		logger.handleEvent(event);
		switch(event.getType()) {
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
		case NO_COMPONENT_FOUND:
		    break;
		case IDENTIFY_TIMEOUT: 
		    break;
		case WARNING:
			break;
		}
         
	}
	
}
