package org.bitrepository.service.exception;

public class WorkflowAbortedException extends Exception {

    public WorkflowAbortedException(String message) {
        super(message);
    }
}
