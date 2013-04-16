package org.bitrepository.service.workflow;

/**
 * Class to identify a workflow instance, based on the collection it belongs to and the workflow name/type 
 */
public class WorkflowID {
    
    private final String collectionID;
    private final String workflowName;
    
    public WorkflowID(String collectionID, String workflowName) {
        this.collectionID = collectionID;
        this.workflowName = workflowName;
    }
    
    public String getWorkflowName() {
        return workflowName;
    }
    
    public String getCollectionID() {
        return collectionID;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((collectionID == null) ? 0 : collectionID.hashCode());
        result = prime * result
                + ((workflowName == null) ? 0 : workflowName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WorkflowID other = (WorkflowID) obj;
        if (collectionID == null) {
            if (other.collectionID != null)
                return false;
        } else if (!collectionID.equals(other.collectionID))
            return false;
        if (workflowName == null) {
            if (other.workflowName != null)
                return false;
        } else if (!workflowName.equals(other.workflowName))
            return false;
        return true;
    }
    
    @Override 
    public String toString() {
        return workflowName + "-" + collectionID;
    }
}
