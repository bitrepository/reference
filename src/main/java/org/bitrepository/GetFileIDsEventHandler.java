package org.bitrepository;

import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.eventhandler.GetFileIDsCompleteEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.eventhandler.OperationFailedEvent;

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
		if(event.getType() == OperationEvent.OperationEventType.Complete) {
			results.done();
		} else if(event.getType() == OperationEvent.OperationEventType.PillarComplete) {
			results.addResultsFromPillar((String) event.getState(), ((FileIDsCompletePillarEvent) event).getFileIDs());	
		}
		
	}
	
	public void handleEvent(FileIDsCompletePillarEvent event) {
		logger.handleEvent(event);
		results.addResultsFromPillar(event.getState(), event.getFileIDs());
	}
	
	public void handleEvent(OperationFailedEvent event) {
		logger.handleEvent(event);
		results.failed();
	}

	public void handleEvent(GetFileIDsCompleteEvent event) {
		logger.handleEvent(event);
		// add results to results object. Does however need to get pillar id first..
		
	}
	
}
