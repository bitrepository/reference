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

import org.bitrepository.protocol.security.exception.MessageSigningException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

/**
 * Handles signing of messages.
 */
public class BasicMessageSigner implements MessageSigner {
    private static final boolean USE_ATTACHED_MODE = false;
    private PrivateKeyEntry privateKeyEntry;
    private JcaSignerInfoGeneratorBuilder builder;
    private ContentSigner sha512Signer;

    /**
     * Sets the privateKeyEntry member and initializes the objects that's needed for signing messages.
     *
     * @param privateKeyEntry the PrivateKeyEntry holding the private key and certificate needed for creating a signature.
     */
    public void setPrivateKeyEntry(PrivateKeyEntry privateKeyEntry) {
        if (privateKeyEntry == null) {
            return;
        }
        this.privateKeyEntry = privateKeyEntry;
        try {
            sha512Signer = new JcaContentSignerBuilder(SecurityModuleConstants.SignatureType).setProvider(SecurityModuleConstants.BC)
                    .build(privateKeyEntry.getPrivateKey());
            builder = new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider(SecurityModuleConstants.BC).build());
            builder.setDirectSignature(true);
        } catch (OperatorCreationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Creates the CMS signature for a message.
     *
     * @param messageData the message data that is to be signed.
     * @return the CMS signature for the message.
     * @throws MessageSigningException in case signing fails.
     */
    @Override
    public byte[] signMessage(byte[] messageData) throws MessageSigningException {
        if (privateKeyEntry == null) {
            throw new MessageSigningException("Private key entry has not been initialized.", null);
        }
        try {
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            gen.addSignerInfoGenerator(builder.build(sha512Signer, (X509Certificate) privateKeyEntry.getCertificate()));
            CMSSignedData signedData = gen.generate(new CMSProcessableByteArray(messageData), USE_ATTACHED_MODE);

            return signedData.getEncoded();
        } catch (Exception e) {
            throw new MessageSigningException(e.getMessage(), e);
        }
    }

}
