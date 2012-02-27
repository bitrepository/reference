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
    
    private final String data = "Hello world!";
    private final String positiveCert = "-----BEGIN CERTIFICATE-----\n" +
            "MIIBuTCCASICCQDMZo0ssJ6s7zANBgkqhkiG9w0BAQUFADAhMQswCQYDVQQG\n" +
            "EwJESzESMBAGA1UEAwwJY2xpZW50LTEzMB4XDTExMTAyMTA5MjAwMVoXDTE0\n" +
            "MDcxNzA5MjAwMVowITELMAkGA1UEBhMCREsxEjAQBgNVBAMMCWNsaWVudC0x\n" +
            "MzCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA6DE31oL3v3tuZilsJ4YK\n" +
            "0fnBRuShVahIh6yTv7BIY6t1+DAT/N+fcnTU73IKGLH+2X67oa3/YhcoySju\n" +
            "Ei0ZehqvTruKH7UAetS2aPsJBiuWX3giJQkhN62E8a5b63A9Aw3iokuoVWd5\n" +
            "Ohm+0Ra+6tcZ/IxWsWRcM8RWjOJb6vcCAwEAATANBgkqhkiG9w0BAQUFAAOB\n" +
            "gQBu3OgpXt/0WluSBmjDPiavLor3lqDoJBGTMn0mr05g0gZFhSfI4vIj5kvW\n" +
            "QUWR/yBgW0chzA+GZHwctaLQyTxp0AT/F4VsTtlN3YpBbeMlOK/BC+w9MpAO\n" +
            "me0coE/bZzOuq3gQ15XOkelIxmnrh2xnGotE6thmFFClT6VY8mqEFA==\n" +
            "-----END CERTIFICATE-----\n";
    private final String negativeCert =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIICCzCCAXQCCQCHLeckUtZcJDANBgkqhkiG9w0BAQUFADBKMQswCQYDVQQGEwJESzEgMB4GA1UE\n" +
            "ChMXRGV0IEtvbmdlbGlnZSBCaWJsaW90ZWsxDDAKBgNVBAsTA0RJUzELMAkGA1UEAxMCQ0EwHhcN\n" +
            "MTEwOTI4MTExNjQ1WhcNMTMwNDI5MjIyMDEzWjBKMQswCQYDVQQGEwJESzEgMB4GA1UEChMXRGV0\n" +
            "IEtvbmdlbGlnZSBCaWJsaW90ZWsxDDAKBgNVBAsTA0RJUzELMAkGA1UEAxMCQ0EwgZ8wDQYJKoZI\n" +
            "hvcNAQEBBQADgY0AMIGJAoGBAJcGvaV2VjjIhq0NGD1sCDPw/Xvu/G0zzJLStStbvAQZ95CKZ52V\n" +
            "CM7oQ4Ge4Qse+sNNL+DU9ENzFoN/1Xvqip1e0B204arErZaRXc4lThW3vTt7JWx9s/l2TOxnsCuq\n" +
            "uXhe+VnQkMdGu1WeSKIgzhxJ5vjV5mPXkj/RsVnKSp+PAgMBAAEwDQYJKoZIhvcNAQEFBQADgYEA\n" +
            "VbQ5VPPDOCW0wuyMLFu8W2W0Tvplv8A458w37qNVo3pvznDSVdEOpPIRznTIM836XSwHWCWhRPN/\n" +
            "Mo2U+CRkSEaN8nPkqxOY46w1AKqhhgLAPr6/sOCjG6k6jxEITYzYO5mv0nAg4yAVvfE4O715pjwO\n" +
            "77h9LapqyJ8S1GSKHr8=\n" +
            "-----END CERTIFICATE-----\n";
    private final String signature = "MIAGCSqGSIb3DQEHAqCAMIACAQExDzANBglghkgBZQMEAgMFADCABgkqhkiG9w0BBwEAADGB1zCB1AIBATAuMCExCzAJBgNVBAYTAkRLMRIwEAYDVQQDDAljbGllbnQtMTMCCQDMZo0ssJ6s7zANBglghkgBZQMEAgMFADANBgkqhkiG9w0BAQEFAASBgHhp9p/wAHX8zAEIamAnyIywpI0wBYvR62pkLIrHwpTgsnjFpJRZPYYiF1egsIcy7ZjQrkh4UtMRLZyGbzk/GeuExdSrj66gAG4j8NeS7Ekp1zb16SUH8bKu/H83PqLxYBvIyEks3lMKu5T76Bmwa9x32H2zpzJjSqLRZCNgwQnBAAAAAAAA";
    
    
    @Test(groups = {"regressiontest"})
    public void positiveCertificateIdentificationTest() throws Exception {
        addDescription("Tests that a certificate can be identified based on the correct signature.");
        addStep("Create CertificateID object based on the certificate used to sign the data", "CertificateID object not null");
        Security.addProvider(new BouncyCastleProvider());
        
        ByteArrayInputStream bs = new ByteArrayInputStream(positiveCert.getBytes("UTF-8"));
        X509Certificate myCertificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(bs);
        CertificateID certificateIDfromCertificate = 
                new CertificateID(myCertificate.getIssuerX500Principal(), myCertificate.getSerialNumber());
        
        addStep("Create CertificateID object based on signature", "Certificate object not null");
        byte[] decodeSig = Base64.decode(signature.getBytes());
        CMSSignedData s = new CMSSignedData(new CMSProcessableByteArray(data.getBytes("UTF-8")), decodeSig);
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
        
        ByteArrayInputStream bs = new ByteArrayInputStream(negativeCert.getBytes("UTF-8"));
        X509Certificate myCertificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(bs);
        CertificateID certificateIDfromCertificate = 
                new CertificateID(myCertificate.getIssuerX500Principal(), myCertificate.getSerialNumber());
        
        addStep("Create CertificateID object based on signature", "Certificate object not null");
        byte[] decodeSig = Base64.decode(signature.getBytes());
        CMSSignedData s = new CMSSignedData(new CMSProcessableByteArray(data.getBytes("UTF-8")), decodeSig);
        SignerInformation signer = (SignerInformation) s.getSignerInfos().getSigners().iterator().next();
        CertificateID certificateIDfromSignature = new CertificateID(signer.getSID().getIssuer(), signer.getSID().getSerialNumber());
        
        addStep("Assert that the two CertificateID objects are equal", "Assert succeeds");
        Assert.assertNotEquals(certificateIDfromCertificate, certificateIDfromSignature);        
    }
}
