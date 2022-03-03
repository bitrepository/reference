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
import org.bitrepository.protocol.security.exception.UnregisteredPermissionException;
import org.bouncycastle.cms.SignerId;

/**
 * Class to authorize an operation based on the certificate which has signed the operation request.  
 */
public interface OperationAuthorizer {

    /**
     * Authorize an operation based on its signature
     * @param operationType operationType, the type of operation that should be authorized.
     * @param signer The signer of the request.
     * @param collectionID The ID of the collection to autorize the operation for.
     * @throws OperationAuthorizationException if the authorization fails.  
     * @throws UnregisteredPermissionException if no permissions could be found for the signer
     */
    void authorizeOperation(String operationType, SignerId signer, String collectionID)
            throws OperationAuthorizationException, UnregisteredPermissionException;
    
    /**
     * Method to determine whether a given componentID is allowed to sign an operation with the given certificate.
     * @param certificateUser the componentID of the component that signed the message
     * @param signer the signerId of the certificate that signed the message
     * @throws CertificateUseException in case the message has been signed by the wrong user. 
     * 
     */
    void authorizeCertificateUse(String certificateUser, SignerId signer) throws CertificateUseException;

}
