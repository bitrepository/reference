package org.bitrepository.protocol.exceptions;

/**
 * Indicates a general failure to complete a operation.
 */
@SuppressWarnings("serial")
public class OperationFailedException extends Exception {

	public OperationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public OperationFailedException(String message) {
		super(message);
	}
}
