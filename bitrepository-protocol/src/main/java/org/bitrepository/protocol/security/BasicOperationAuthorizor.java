package org.bitrepository.protocol.security;

import java.util.List;

import org.bitrepository.settings.collectionsettings.OperationPermission;
import org.bouncycastle.cms.SignerId;

/**
 * Class to check permissions based on the signer of a MessageRequest and the type of request. 
 */
public class BasicOperationAuthorizor implements OperationAuthorizor {

    /** Mapper from operation type to needed permission */
    private RequestToOperationPermissionMapper requestToPermissionMapper;
    /** Non-infrastructure certificates and permission store */
    private final PermissionStore permissionStore;
    
    /**
     * Public constructor
     * @param PermissionStore permissionStore which holds the permissions to check against. 
     */
    public BasicOperationAuthorizor(PermissionStore permissionStore) {
        requestToPermissionMapper = new RequestToOperationPermissionMapper();
        this.permissionStore = permissionStore;
    }
    
    /**
     * Method to determine whether an operation is allow
     * @param operationType, the type of operation to authorize 
     * @param signer the signerId of the certificate used to create the signature belonging to the request 
     * which is to be authorized.  
     * @throws OperationAuthorizationException if authorization fails
     */
    @Override
    public void authorizeOperation(String operationType, SignerId signer) throws OperationAuthorizationException {
        List<OperationPermission> permissions = requestToPermissionMapper.getRequiredPermissions(operationType);
        for(OperationPermission permission : permissions) {
            try {
                if(permissionStore.checkPermission(signer, permission)) {
                    return ;
                }
            } catch (PermissionStoreException e) {
                throw new OperationAuthorizationException(e.getMessage(), e);
            }
        }
        throw new OperationAuthorizationException("The required permission has not been registered for the signer: " + 
                signer.toString());
    }

}
