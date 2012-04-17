package org.bitrepository;

import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.eventhandler.OperationEvent;

/**
 *	Event handler for the asynchronous GetFileIDs method.   
 */
public class GetFileIDsEventHandler implements EventHandler {

	private EventHandler logger;
	private GetFileIDsResults results;
	
	public GetFileIDsEventHandler(GetFileIDsResults results, EventHandler logger) {
		this.logger = logger;
		this.results = results;
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
		    results.addResultsFromPillar(((FileIDsCompletePillarEvent) event).getContributorID(), 
		            ((FileIDsCompletePillarEvent) event).getFileIDs());
		    break;
		case COMPLETE:
			results.done();
		    break;
		case COMPONENT_FAILED:
		    break;
		case FAILED:
		    results.failed();
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
