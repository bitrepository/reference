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

/**
 * Class containing empty / safe implementation of the SecurityManager interface.
 * It is intented to be used in tests, or where the functionality of a real SecurityManager implementation is not
 * needed. 
 */
public class DummySecurityManager implements SecurityManager {

    @Override
    public void authenticateMessage(String message, String signature) throws MessageAuthenticationException {
        // Safe empty implementation
    }

    @Override
    public String signMessage(String message) throws MessageSigningException {
        // Safe empty implementation
        return null;
    }

    @Override
    public void authorizeOperation(String operationType, String messageData, String signature) 
            throws OperationAuthorizationException {
        // Safe empty implementation
    }

    @Override
    public void authorizeCertificateUse(String certificateUser, String messageData, String signature)
            throws CertificateUseException {
        // Safe empty implementation
    }

}
