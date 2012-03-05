package org.bitrepository.protocol.security;

import java.io.ByteArrayInputStream;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CertificateIDTest extends ExtendedTestCase  {
        
    @Test(groups = {"regressiontest"})
    public void positiveCertificateIdentificationTest() throws Exception {
        addDescription("Tests that a certificate can be identified based on the correct signature.");
        addStep("Create CertificateID object based on the certificate used to sign the data", "CertificateID object not null");
        Security.addProvider(new BouncyCastleProvider());
        
        ByteArrayInputStream bs = new ByteArrayInputStream(
                SecurityTestConstants.getPositiveCertificate().getBytes(SecurityModuleConstants.defaultEncodingType));
        X509Certificate myCertificate = (X509Certificate) CertificateFactory.getInstance(
                SecurityModuleConstants.CertificateType).generateCertificate(bs);
        CertificateID certificateIDfromCertificate = 
                new CertificateID(myCertificate.getIssuerX500Principal(), myCertificate.getSerialNumber());
        
        addStep("Create CertificateID object based on signature", "Certificate object not null");
        byte[] decodeSig = Base64.decode(SecurityTestConstants.getSignature().getBytes());
        CMSSignedData s = new CMSSignedData(new CMSProcessableByteArray(
                SecurityTestConstants.getTestData().getBytes(SecurityModuleConstants.defaultEncodingType)), decodeSig);
        SignerInformation signer = (SignerInformation) s.getSignerInfos().getSigners().iterator().next();
        CertificateID certificateIDfromSignature = new CertificateID(signer.getSID().getIssuer(), signer.getSID().getSerialNumber());
        
        addStep("Assert that the two CertificateID objects are equal", "Assert succeeds");
        Assert.assertEquals(certificateIDfromCertificate, certificateIDfromSignature);
    }
    
    @Test(groups = {"regressiontest"})
    public void negativeCertificateIdentificationTest() throws Exception {
        addDescription("Tests that a certificate is not identified based on a incorrect signature.");
        addStep("Create CertificateID object based on a certificate not used for signing the data", "CertificateID object not null");
        Security.addProvider(new BouncyCastleProvider());
        
        ByteArrayInputStream bs = new ByteArrayInputStream(
                SecurityTestConstants.getNegativeCertificate().getBytes(SecurityModuleConstants.defaultEncodingType));
        X509Certificate myCertificate = (X509Certificate) CertificateFactory.getInstance(
                SecurityModuleConstants.CertificateType).generateCertificate(bs);
        CertificateID certificateIDfromCertificate = 
                new CertificateID(myCertificate.getIssuerX500Principal(), myCertificate.getSerialNumber());
        
        addStep("Create CertificateID object based on signature", "Certificate object not null");
        byte[] decodeSig = Base64.decode(SecurityTestConstants.getSignature().getBytes());
        CMSSignedData s = new CMSSignedData(new CMSProcessableByteArray(
                SecurityTestConstants.getTestData().getBytes(SecurityModuleConstants.defaultEncodingType)), decodeSig);
        SignerInformation signer = (SignerInformation) s.getSignerInfos().getSigners().iterator().next();
        CertificateID certificateIDfromSignature = new CertificateID(signer.getSID().getIssuer(), signer.getSID().getSerialNumber());
        
        addStep("Assert that the two CertificateID objects are equal", "Assert succeeds");
        Assert.assertNotEquals(certificateIDfromCertificate, certificateIDfromSignature);        
    }
}
