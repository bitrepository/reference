package org.bitrepository.protocol.security;

import java.io.IOException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

/**
 * Class to handle signing of messages.  
 */
public class BasicMessageSigner implements MessageSigner {

    private PrivateKeyEntry privateKeyEntry;
    private JcaSignerInfoGeneratorBuilder builder;
    private ContentSigner sha512Signer;
    
    /**
     * Sets the privateKeyEntry member and initializes the objects that's needed for signing messages.
     * @param PrivateKeyEntry the PrivatKeyEntry holding the private key and certificate needed for creating a signature.   
     */
    public void setPrivateKeyEntry(PrivateKeyEntry privateKeyEntry) {
        if(privateKeyEntry == null) {
            return;
        }
        this.privateKeyEntry = privateKeyEntry;
        try {
            sha512Signer = new JcaContentSignerBuilder("SHA512withRSA").setProvider("BC").build(
                    privateKeyEntry.getPrivateKey());
            builder = new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider("BC").build());
            builder.setDirectSignature(true);
        } catch (OperatorCreationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /**
     * Creates the CMS signature for a message. 
     * @param byte[] messageData, the message data that is to be signed. 
     * @return byte[] the CMS signature for the message. 
     * @throws MessageSigningException in case signing fails. 
     */
    @Override
    public byte[] signMessage(byte[] messageData) throws MessageSigningException {
        if(privateKeyEntry == null) {
            throw new MessageSigningException("Private key entry has not been initialized.", null);
        }
        try {
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            gen.addSignerInfoGenerator(builder.build(sha512Signer, (X509Certificate)privateKeyEntry.getCertificate()));
            CMSSignedData signedData = gen.generate(new CMSProcessableByteArray(messageData), false /*detached*/);

            return signedData.getEncoded();
        } catch (OperatorCreationException e) {
            throw new MessageSigningException(e.getMessage(), e);
        } catch (CertificateEncodingException e) {
            throw new MessageSigningException(e.getMessage(), e);
        } catch (CMSException e) {
            throw new MessageSigningException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MessageSigningException(e.getMessage(), e);
        }
    }

}
