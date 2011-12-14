package org.bitrepository;

import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.eventhandler.OperationEvent;

/**
 *	Event handler for the asynchronous GetFileIDs method.   
 */
public class GetChecksumsEventHandler implements EventHandler {

	private EventHandler logger;
	private GetChecksumsResults results;
	
	public GetChecksumsEventHandler(GetChecksumsResults results, EventHandler logger) {
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
		            ((ChecksumsCompletePillarEvent) event).getChecksums());
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
