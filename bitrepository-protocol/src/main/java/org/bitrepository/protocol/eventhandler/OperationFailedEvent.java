package org.bitrepository.protocol.eventhandler;

import org.bitrepository.protocol.exceptions.OperationFailedException;

/**
 * Indicates and operation has failed to complete
 */
public class OperationFailedEvent implements OperationEvent<OperationFailedException> {
	private final String info;
	private final OperationEventType type = OperationEventType.Failed;
	private final OperationFailedException exception;
	
	/**
	 * Constructor with exception information
	 * @param info
	 * @param exception
	 */
	public OperationFailedEvent(String info, OperationFailedException exception) {
		super();
		this.info = info;
		this.exception = exception;
	}
	/**
	 * Plain info constructor.
	 * @param info
	 */
	public OperationFailedEvent(String info) {
		super();
		this.info = info;
		this.exception = null;
	}

	@Override
	public String getInfo() {
		return info;
	}

	@Override
	public org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType getType() {
		return type;
	}

	@Override
	public OperationFailedException getState() {
		return exception;
	}
}
