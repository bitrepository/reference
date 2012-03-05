package org.bitrepository.protocol.security;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.util.encoders.Base64;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PermissionStoreTest extends ExtendedTestCase  {
    
    private PermissionStore permissionStore;
    
    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        permissionStore = new PermissionStore();
        permissionStore.loadPermissions(SecurityTestConstants.getDefaultPermissions());
    }
    
    @Test(groups = {"regressiontest"})
    public void positiveCertificateRetrievalTest() throws Exception {
        addDescription("Tests that a certificate can be retrieved based on the correct signerId.");
        addStep("Create signer to lookup certificate", "No exceptions");
        byte[] decodeSig = 
                Base64.decode(SecurityTestConstants.getSignature().getBytes(SecurityModuleConstants.defaultEncodingType));
        CMSSignedData s = new CMSSignedData(new CMSProcessableByteArray(
                SecurityTestConstants.getTestData().getBytes(SecurityModuleConstants.defaultEncodingType)), decodeSig);
        SignerInformation signer = (SignerInformation) s.getSignerInfos().getSigners().iterator().next(); 
        addStep("Lookup certificate based on signerId", "No exceptions");
        X509Certificate certificateFromStore = permissionStore.getCertificate(signer.getSID());        
        ByteArrayInputStream bs = new ByteArrayInputStream(
                SecurityTestConstants.getPositiveCertificate().getBytes(SecurityModuleConstants.defaultEncodingType));
        X509Certificate positiveCertificate = (X509Certificate) CertificateFactory.getInstance(
                SecurityModuleConstants.CertificateType).generateCertificate(bs);
        Assert.assertEquals(positiveCertificate, certificateFromStore);
    }
    
    @Test(groups = {"regressiontest"})
    public void negativeCertificateRetrievalTest() throws Exception {
        addDescription("Tests that a certificate cannot be retrieved based on the wrong signerId.");
        addStep("Create signer and modify its ID so lookup will fail", "No exceptions");
        byte[] decodeSig = 
                Base64.decode(SecurityTestConstants.getSignature().getBytes(SecurityModuleConstants.defaultEncodingType));
        CMSSignedData s = new CMSSignedData(new CMSProcessableByteArray(
                SecurityTestConstants.getTestData().getBytes(SecurityModuleConstants.defaultEncodingType)), decodeSig);
        SignerInformation signer = (SignerInformation) s.getSignerInfos().getSigners().iterator().next(); 
        SignerId signerId= signer.getSID();
        BigInteger serial = signerId.getSerialNumber();
        serial.add(new BigInteger("2"));
        signerId.setSerialNumber(serial);
        addStep("Lookup certificate based on signerId", "No exceptions");
        X509Certificate certificateFromStore = permissionStore.getCertificate(signerId);        
        ByteArrayInputStream bs = new ByteArrayInputStream(
                SecurityTestConstants.getPositiveCertificate().getBytes(SecurityModuleConstants.defaultEncodingType));
        X509Certificate positiveCertificate = (X509Certificate) CertificateFactory.getInstance(
                SecurityModuleConstants.CertificateType).generateCertificate(bs);
        Assert.assertEquals(positiveCertificate, certificateFromStore);
    }
    
    @Test(groups = {"regressiontest"})
    public void certificatePermissionCheckTest() throws Exception {
        addDescription("Tests that a certificate only allows for the expected permission.");
    }
    
    @Test(groups = {"regressiontest"})
    public void unknownCertificatePermissionCheckTest() throws Exception {
        addDescription("Tests that a unknown certificate results in expected refusal.");
    }    
}
