package org.bitrepository.protocol.eventhandler;


/**
 * Event for a specific pillar.
 */
public class PillarOperationEvent implements OperationEvent<String> {
	private final OperationEventType type;
	private final String info;
	private final String pillarID;
	
	/**
	 * Constructor with exception information
	 * @param type The event type
	 * @param info Free text description of the event
	 * @param pillarID The ID of the pillar this event relates to
	 */
	public PillarOperationEvent(OperationEventType type, String info, String pillarID) {
		super();
		this.type = type;
		this.info = info;
		this.pillarID = pillarID;
	}

	@Override
	public String getInfo() {
		return info;
	}

	@Override
	public org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType getType() {
		return type;
	}

	/**
	 * Returns the ID of the pillar this event relates to.
	 */
	@Override
	public String getState() {
		return pillarID;
	}
}
