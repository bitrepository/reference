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

import java.util.List;

import org.bitrepository.settings.collectionsettings.Operation;
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
     * @throws UnregisteredPermissionException 
     */
    @Override
    public void authorizeOperation(String operationType, SignerId signer) throws OperationAuthorizationException, 
            UnregisteredPermissionException {
        List<Operation> permissions = requestToPermissionMapper.getRequiredPermissions(operationType);
        for(Operation permission : permissions) {
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
