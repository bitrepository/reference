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
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.settings.collectionsettings.OperationPermission;

/**
 * Class to map between a Operation/RequestType (Bitrepository message request type) and
 * the permissions required to allowed execution of the operation. 
 */
public class RequestToOperationPermissionMapper {
    /** Mapping from MessageRequest type to a list of permissions */
    private final Map<String, List<OperationPermission>> mapping;

    /**
     * Constructor for the class. Initiates the default permission mappings.  
     */
    public RequestToOperationPermissionMapper() {
        mapping = new HashMap<String, List<OperationPermission>>();
        ArrayList<OperationPermission> requiredPermissions = new ArrayList<OperationPermission>();
        requiredPermissions.add(OperationPermission.ALL);
        requiredPermissions.add(OperationPermission.GET_FILE);
        mapping.put(IdentifyPillarsForGetFileRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(GetFileRequest.class.getSimpleName(), requiredPermissions);
        requiredPermissions = new ArrayList<OperationPermission>();
        requiredPermissions.add(OperationPermission.ALL);
        requiredPermissions.add(OperationPermission.PUT_FILE);
        mapping.put(IdentifyPillarsForPutFileRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(PutFileRequest.class.getSimpleName(), requiredPermissions);
        requiredPermissions = new ArrayList<OperationPermission>();
        requiredPermissions.add(OperationPermission.ALL);
        requiredPermissions.add(OperationPermission.GET_FILE_I_DS);
        mapping.put(IdentifyPillarsForGetFileIDsRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(GetFileIDsRequest.class.getSimpleName(), requiredPermissions);
        requiredPermissions = new ArrayList<OperationPermission>();
        requiredPermissions.add(OperationPermission.ALL);
        requiredPermissions.add(OperationPermission.GET_CHECKSUMS);
        mapping.put(IdentifyPillarsForGetChecksumsRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(GetChecksumsRequest.class.getSimpleName(), requiredPermissions);
        requiredPermissions = new ArrayList<OperationPermission>();
        requiredPermissions.add(OperationPermission.ALL);
        requiredPermissions.add(OperationPermission.GET_AUDIT_TRAILS);
        mapping.put(IdentifyContributorsForGetAuditTrailsRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(GetAuditTrailsRequest.class.getSimpleName(), requiredPermissions);
        requiredPermissions = new ArrayList<OperationPermission>();
        requiredPermissions.add(OperationPermission.ALL);
        requiredPermissions.add(OperationPermission.DELETE_FILE);
        mapping.put(IdentifyPillarsForDeleteFileRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(DeleteFileRequest.class.getSimpleName(), requiredPermissions);
        requiredPermissions = new ArrayList<OperationPermission>();
        requiredPermissions.add(OperationPermission.ALL);
        requiredPermissions.add(OperationPermission.REPLACE_FILE);
        mapping.put(IdentifyPillarsForReplaceFileRequest.class.getSimpleName(), requiredPermissions);
        mapping.put(ReplaceFileRequest.class.getSimpleName(), requiredPermissions);
    }

    /**
     * Get the required permission for the given operation type (bitrepository message request type) 
     * @throws UnregisteredPermissionException 
     */
    public List<OperationPermission> getRequiredPermissions(String operationType) throws UnregisteredPermissionException {
        List<OperationPermission> permissions = mapping.get(operationType);
        if(permissions == null) {
            throw new UnregisteredPermissionException("No permissions has been registered for: " + operationType);
        }
        return permissions;
    }
}


