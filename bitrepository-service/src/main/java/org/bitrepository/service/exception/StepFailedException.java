package org.bitrepository.service.exception;

public class StepFailedException extends Exception {
    
    public StepFailedException(String message, Exception e) {
        super(message, e);
    }
}
