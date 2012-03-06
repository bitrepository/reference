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

import java.security.cert.X509Certificate;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.encoders.Base64;

/**
 * Class to handle authentication of messages.  
 */
public class BasicMessageAuthenticator implements MessageAuthenticator {

    /**
     * Non-infrastructure certificates and permission store  
     */
    private final PermissionStore permissionStore;
    
    /**
     * Public constructor
     * @param PermissionStore the permissionStore holding the known certificates 
     */
    public BasicMessageAuthenticator(PermissionStore permissionStore) {
        this.permissionStore = permissionStore;
    }
    
    /**
     * Attempts to authenticate the message based on a signature. 
     * @param messageData, the data that is to be authenticated
     * @param signatureData, the signature that of the data to be authenticated.
     * @throws MessageAuthenticationException in case authentication fails.  
     */
    @Override
    public void authenticateMessage(byte[] messageData, byte[] signatureData) throws MessageAuthenticationException {
        X509Certificate signingCert;
        try {
     //       byte[] decodedSig = Base64.decode(signatureData); 
            CMSSignedData s = new CMSSignedData(new CMSProcessableByteArray(messageData), signatureData);
            SignerInformation signer = (SignerInformation) s.getSignerInfos().getSigners().iterator().next();
            signingCert = permissionStore.getCertificate(signer.getSID());
            
            if(!signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(SecurityModuleConstants.BC).build(signingCert))) {
                throw new MessageAuthenticationException("Signature does not match the message. Indicated certificate " +
                        "did not sign message. Certificate issuer: " + signingCert.getIssuerX500Principal().getName() +
                        ", serial: " + signingCert.getSerialNumber());  
            }
        } catch (OperatorCreationException e) {
            throw new RuntimeException(e);
        } catch (CMSException e) {
            throw new RuntimeException(e);
        } catch (PermissionStoreException e) {
            throw new MessageAuthenticationException(e.getMessage(), e);
        }
    }

}
