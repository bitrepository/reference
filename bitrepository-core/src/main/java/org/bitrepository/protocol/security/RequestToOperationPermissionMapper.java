/*
 * #%L
 * Bitrepository Protocol
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.protocol.security.exception.UnregisteredPermissionException;
import org.bitrepository.settings.repositorysettings.Operation;

/**
 * Class to map between a Operation/RequestType (Bitrepository message request type) and
 * the permissions required to allowed execution of the operation. 
 */
public class RequestToOperationPermissionMapper {
    /** Mapping from MessageRequest type to a list of permissions */
    private final Map<String, List<Operation>> mapping;

    /**
     * Constructor for the class. Initiates the default permission mappings.  
     */
    public RequestToOperationPermissionMapper() {
        mapping = new HashMap<String, List<Operation>>();
        ArrayList<Operation> requiredPermissions = new ArrayList<Operation>();
        requiredPermissions.add(Operation.ALL);
        requiredPermissions.add(Operation.GET_FILE);
        mapping.put(IdentifyPillarsForGetFileRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(GetFileRequest.class.getSimpleName(), requiredPermissions);
        
        requiredPermissions = new ArrayList<Operation>();
        requiredPermissions.add(Operation.ALL);
        requiredPermissions.add(Operation.PUT_FILE);
        mapping.put(IdentifyPillarsForPutFileRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(PutFileRequest.class.getSimpleName(), requiredPermissions);
        
        requiredPermissions = new ArrayList<Operation>();
        requiredPermissions.add(Operation.ALL);
        requiredPermissions.add(Operation.GET_FILE_I_DS);
        mapping.put(IdentifyPillarsForGetFileIDsRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(GetFileIDsRequest.class.getSimpleName(), requiredPermissions);
        
        requiredPermissions = new ArrayList<Operation>();
        requiredPermissions.add(Operation.ALL);
        requiredPermissions.add(Operation.GET_CHECKSUMS);
        mapping.put(IdentifyPillarsForGetChecksumsRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(GetChecksumsRequest.class.getSimpleName(), requiredPermissions);
        
        requiredPermissions = new ArrayList<Operation>();
        requiredPermissions.add(Operation.ALL);
        requiredPermissions.add(Operation.GET_AUDIT_TRAILS);
        mapping.put(IdentifyContributorsForGetAuditTrailsRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(GetAuditTrailsRequest.class.getSimpleName(), requiredPermissions);
        
        requiredPermissions = new ArrayList<Operation>();
        requiredPermissions.add(Operation.ALL);
        requiredPermissions.add(Operation.DELETE_FILE);
        mapping.put(IdentifyPillarsForDeleteFileRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(DeleteFileRequest.class.getSimpleName(), requiredPermissions);
        
        requiredPermissions = new ArrayList<Operation>();
        requiredPermissions.add(Operation.ALL);
        requiredPermissions.add(Operation.REPLACE_FILE);
        mapping.put(IdentifyPillarsForReplaceFileRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(ReplaceFileRequest.class.getSimpleName(), requiredPermissions);

        requiredPermissions = new ArrayList<Operation>();
        requiredPermissions.add(Operation.ALL);
        requiredPermissions.add(Operation.GET_STATUS);
        mapping.put(IdentifyContributorsForGetStatusRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(GetStatusRequest.class.getSimpleName(), requiredPermissions);
    }

    /**
     * Get the required permission for the given operation type (bitrepository message request type) 
     * @throws UnregisteredPermissionException 
     */
    public List<Operation> getRequiredPermissions(String operationType) throws UnregisteredPermissionException {
        List<Operation> permissions = mapping.get(operationType);
        if(permissions == null) {
            throw new UnregisteredPermissionException("No permissions has been registered for: " + operationType);
        }
        return permissions;
    }
}


