package org.bitrepository.protocol.security;

import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

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

    /** Constant to indicate whether to used attached or detached mode for signing.*/
    private final static boolean USE_ATTACHED_MODE = false;
    /** Container for the private key and certificate needed to sign messages*/
    private PrivateKeyEntry privateKeyEntry;
    /** SignerInfoBuilder used in the signing process. */
    private JcaSignerInfoGeneratorBuilder builder;
    /** Content signer used to sign messages */
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
            sha512Signer = new JcaContentSignerBuilder(SecurityModuleConstants.SignatureType)
                    .setProvider(SecurityModuleConstants.BC).build(privateKeyEntry.getPrivateKey());
            builder = new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider(SecurityModuleConstants.BC).build());
            builder.setDirectSignature(true);
        } catch (OperatorCreationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /**
     * Creates the CMS signature for a message. 
     * @param messageData, the message data that is to be signed. 
     * @return the CMS signature for the message. 
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
            CMSSignedData signedData = gen.generate(new CMSProcessableByteArray(messageData), USE_ATTACHED_MODE);

            return signedData.getEncoded();
        } catch (Exception e) {
            throw new MessageSigningException(e.getMessage(), e);
        }
    }

}
