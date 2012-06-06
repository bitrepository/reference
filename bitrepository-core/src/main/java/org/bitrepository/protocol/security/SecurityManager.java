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
import org.bitrepository.protocol.security.exception.MessageAuthenticationException;
import org.bitrepository.protocol.security.exception.MessageSigningException;
import org.bitrepository.protocol.security.exception.OperationAuthorizationException;

public interface SecurityManager {
    /**
     * Method to authenticate a message. 
     * @param message, the message that needs to be authenticated. 
     * @param signature, the signature belonging to the message. 
     * @throws MessageAuthenticationException in case of failure.
     */
    void authenticateMessage(String message, String signature) throws MessageAuthenticationException;
    
    /**
     * Method to sign a message
     * @param message, the message to sign
     * @return String the signature for the message, or null if authentication is disabled. 
     * @throws MessageSigningException if signing of the message fails.   
     */
    String signMessage(String message) throws MessageSigningException;
    
    /**
     * Method to authorize an operation 
     * @param operationType, the type of operation that is to be authorized. 
     * @param messageData, the data of the message request. 
     * @param signature, the signature belonging to the message request. 
     * @throws OperationAuthorizationException in case of failure. 
     */
    void authorizeOperation(String operationType, String messageData, String signature) 
            throws OperationAuthorizationException;
    
    /** 
     * Method to authorize the use of a certificate
     * @param certificateUser, the user who signed the message
     * @param messageData, the data of the message request. 
     * @param signature, the signature belonging to the message request. 
     * @throws CertificateUseException in case the certificate use could not be authorized. 
     */
    void authorizeCertificateUse(String certificateUser, String messageData, String signature) 
            throws CertificateUseException;
}
