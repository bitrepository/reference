package org.bitrepository.service.exception;

/**
 * WorkflowAbortedException allows to communicate to an upstream fault barrier that the workflow aborted.
 */
public class WorkflowAbortedException extends Exception {

    public WorkflowAbortedException(String message) {
        super(message);
    }
}
