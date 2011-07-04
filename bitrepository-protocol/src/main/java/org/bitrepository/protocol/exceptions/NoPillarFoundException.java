package org.bitrepository.protocol.exceptions;

/**
 * Indicates a failure find a suitable pillar after dispatching a IdentidyPillarsRequest.
 */
@SuppressWarnings("serial")
public class NoPillarFoundException extends OperationFailedException {

	public NoPillarFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NoPillarFoundException(String message) {
		super(message);
	}
}
