package org.bitrepository.protocol.exceptions;

/**
 * Indicates a failure to complete a operation before the configured timeout was reached.
 */
@SuppressWarnings("serial")
public class OperationTimeOutException extends OperationFailedException {

	public OperationTimeOutException(String message, Throwable cause) {
		super(message, cause);
	}

	public OperationTimeOutException(String message) {
		super(message);
	}
}
