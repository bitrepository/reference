package org.bitrepository.service.exception;

/**
 * StepFailedException allows communicating to the upstream fault barrier that a step failed.
 */
public class StepFailedException extends Exception {

    public StepFailedException(String message, Exception e) {
        super(message, e);
    }
}
