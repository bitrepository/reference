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
	
	@Override
	public void handleEvent(OperationEvent event) {
		logger.handleEvent(event);

		switch(event.getType()) {
		case IdentifyPillarsRequestSent:
		    break;
		case PillarIdentified:
		    break;
		case PillarSelected:
		    break;
		case RequestSent:
		    break;
		case Progress:
		    break;
		case PillarComplete:
		    break;
		case Complete:
		    completedFiles.add(url);
		    break;
		case PillarFailed:
		    break;
		case Failed:
		    break;
		case NoPillarFound:
		    break;
		case IdentifyPillarTimeout: 
		    break;
		case Warning:
		    break;
		}
         
	}
	
}
