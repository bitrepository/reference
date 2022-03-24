/*
 * #%L
 * Bitrepository Service
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.service.workflow;

/**
 * Class to identify a workflow instance, based on the collection it belongs to and the workflow name/type.
 */
public class JobID {
    private final String collectionID;
    private final String workflowName;

    public JobID(String workflowName, String collectionID) {
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
        JobID other = (JobID) obj;
        if (collectionID == null) {
            if (other.collectionID != null)
                return false;
        } else if (!collectionID.equals(other.collectionID))
            return false;
        if (workflowName == null) {
            return other.workflowName == null;
        } else return workflowName.equals(other.workflowName);
    }

    @Override
    public String toString() {
        return workflowName + "-" + collectionID;
    }
}
