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

import org.bitrepository.protocol.security.exception.CertificateUseException;
import org.bitrepository.protocol.security.exception.OperationAuthorizationException;
import org.bitrepository.protocol.security.exception.PermissionStoreException;
import org.bitrepository.protocol.security.exception.UnregisteredPermissionException;
import org.bitrepository.settings.repositorysettings.Operation;
import org.bouncycastle.cms.SignerId;

import java.util.List;

/**
 * Class to check permissions based on the signer of a MessageRequest and the type of request.
 */
public class BasicOperationAuthorizer implements OperationAuthorizer {
    private final RequestToOperationPermissionMapper requestToPermissionMapper;
    private final PermissionStore permissionStore;

    /**
     * @param permissionStore permissionStore which holds the permissions to check against.
     */
    public BasicOperationAuthorizer(PermissionStore permissionStore) {
        requestToPermissionMapper = new RequestToOperationPermissionMapper();
        this.permissionStore = permissionStore;
    }

    /**
     * Method to determine whether a given componentID is allowed to sign a message with the given certificate.
     *
     * @param certificateUser the componentID of the component that signed the message
     * @param signer          the signerId of the certificate that signed the message
     * @throws CertificateUseException in case the message has been signed by the wrong user.
     */
    public void authorizeCertificateUse(String certificateUser, SignerId signer) throws CertificateUseException {
        try {
            if (permissionStore.checkCertificateUser(signer, certificateUser)) {
                return;
            }
        } catch (PermissionStoreException e) {
            throw new CertificateUseException(e.getMessage(), e);
        }
        throw new CertificateUseException(
                "The user '" + certificateUser + "' does not have registered the needed " + "rights for being the signer: " + signer);
    }

    /**
     * Method to determine whether an operation is allowed
     *
     * @param operationType the type of operation to authorize
     * @param signer        the signerId of the certificate used to create the signature belonging to the request
     *                      which is to be authorized.
     * @param collectionID  The ID to authorize the operation for.
     * @throws OperationAuthorizationException if authorization fails
     */
    @Override
    public void authorizeOperation(String operationType, SignerId signer, String collectionID)
            throws OperationAuthorizationException, UnregisteredPermissionException {
        List<Operation> permissions = requestToPermissionMapper.getRequiredPermissions(operationType);
        for (Operation permission : permissions) {
            try {
                if (permissionStore.checkPermission(signer, permission, collectionID)) {
                    return;
                }
            } catch (PermissionStoreException e) {
                throw new OperationAuthorizationException(e.getMessage(), e);
            }
        }
        throw new OperationAuthorizationException("The required permission has not been registered for the signer: " + signer.toString());
    }

}
