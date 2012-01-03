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
		    results.addResultsFromPillar((String) event.getState(), 
		            ((FileIDsCompletePillarEvent) event).getFileIDs());
		    break;
		case Complete:
		    results.done();
		    break;
		case PillarFailed:
		    break;
		case Failed:
		    results.failed();
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
