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

import org.bitrepository.protocol.security.exception.MessageAuthenticationException;
import org.bitrepository.protocol.security.exception.PermissionStoreException;
import org.bitrepository.protocol.security.exception.SecurityException;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.operator.OperatorCreationException;

import java.security.cert.X509Certificate;

/**
 * Class to handle authentication of messages.
 */
public class BasicMessageAuthenticator implements MessageAuthenticator {

    /**
     * Non-infrastructure certificates and permission store
     */
    private final PermissionStore permissionStore;

    /**
     * @param permissionStore the permissionStore holding the known certificates
     */
    public BasicMessageAuthenticator(PermissionStore permissionStore) {
        this.permissionStore = permissionStore;
    }

    @Override
    public SignerId authenticateMessage(byte[] messageData, byte[] signatureData) throws MessageAuthenticationException {
        try {
            CMSSignedData s = new CMSSignedData(new CMSProcessableByteArray(messageData), signatureData);
            SignerInformation signer = s.getSignerInfos().getSigners().iterator().next();
            X509Certificate signingCert = permissionStore.getCertificate(signer.getSID());
            SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder().setProvider(
                    SecurityModuleConstants.BC).build(signingCert);

            if (!signer.verify(verifier)) {
                throw new MessageAuthenticationException("Signature does not match the message. Indicated " +
                        "certificate did not sign message. Certificate issuer: "
                        + signingCert.getIssuerX500Principal().getName() + ", serial: "
                        + signingCert.getSerialNumber());
            }
            return signer.getSID();
        } catch (PermissionStoreException e) {
            throw new MessageAuthenticationException(e.getMessage(), e);
        } catch (CMSException | OperatorCreationException e) {
            throw new SecurityException(e.getMessage(), e);
        }
    }
}
