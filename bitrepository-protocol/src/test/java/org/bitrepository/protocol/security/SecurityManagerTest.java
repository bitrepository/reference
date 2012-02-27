package org.bitrepository.protocol.security;

import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.settings.collectionsettings.OperationPermission;
import org.bitrepository.settings.collectionsettings.Permission;
import org.bitrepository.settings.collectionsettings.PermissionSet;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.util.encoders.Base64;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SecurityManagerTest extends ExtendedTestCase  {
    
    private static final String testData = "Hello world!";
    
    private static final String signature = "MIAGCSqGSIb3DQEHAqCAMIACAQExDzANBglghkgBZQMEAgMFADCABgkqhkiG9w0BBwEAADGB1zCB1AIBATAuMCExCzAJBgNVBAYTAkRLMRIwEAYDVQQDDAljbGllbnQtMTMCCQDMZo0ssJ6s7zANBglghkgBZQMEAgMFADANBgkqhkiG9w0BAQEFAASBgHhp9p/wAHX8zAEIamAnyIywpI0wBYvR62pkLIrHwpTgsnjFpJRZPYYiF1egsIcy7ZjQrkh4UtMRLZyGbzk/GeuExdSrj66gAG4j8NeS7Ekp1zb16SUH8bKu/H83PqLxYBvIyEks3lMKu5T76Bmwa9x32H2zpzJjSqLRZCNgwQnBAAAAAAAA";
    
    private static final String positiveCert = "-----BEGIN CERTIFICATE-----\n" +
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
    
    private static final String negativeCert =
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
    
    private static final String KEYFILE = "./target/test-classes/client-19.pem";
    private SecurityManager securityManager;
    
    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
        MessageSigner messageSigner = new BasicMessageSigner();
        Settings settings = TestSettingsProvider.reloadSettings();
        settings.getCollectionSettings().setPermissionSet(getDefaultPermissions());
        securityManager = new SecurityManager(settings.getCollectionSettings(), 
                        KEYFILE, 
                        authenticator, 
                        messageSigner, 
                        authorizer, 
                        permissionStore);
    }
    
    @Test(groups = {"regressiontest"})
    public void authorizationBehaviourTest() throws Exception {
        addDescription("Tests that a signature only allows the correct requests.");
        addStep("Check that GET_FILE is allowed.", "GET_FILE is allowed.");
        
        byte[] decodeSig = Base64.decode(signature.getBytes("UTF-8"));
        CMSSignedData s = new CMSSignedData(new CMSProcessableByteArray(testData.getBytes("UTF-8")), decodeSig);
        SignerInformation signer = (SignerInformation) s.getSignerInfos().getSigners().iterator().next();        
        
        try {
            securityManager.authorizeOperation(GetFileRequest.class.getSimpleName(), testData, signature);
        } catch (OperationAuthorizationException e) {
            Assert.fail(e.getMessage());
        }
               
        try {
            securityManager.authorizeOperation(PutFileRequest.class.getSimpleName(), testData, signature);
            Assert.fail("SecurityManager did not throw the expected exception");
        } catch (OperationAuthorizationException e) {
            
        }
        
        
    }        

    @Test(groups = {"regressiontest"})
    public void positiveSigningAuthenticationRoundtripTest() throws Exception {
        addDescription("Tests that a roundtrip of signing a request and afterwards authenticating is succedes.");
    }    

    @Test(groups = {"regressiontest"})
    public void negativeSigningAuthenticationRoundtripUnkonwnCertificateTest() throws Exception {
        addDescription("Tests that a roundtrip of signing a request and afterwards authenticating it fails due to " +
        		"a unknown certificate.");
    }   
    
    @Test(groups = {"regressiontest"})
    public void negativeSigningAuthenticationRoundtripBadDataTest() throws Exception {
        addDescription("Tests that a roundtrip of signing a request and afterwards authenticating it fails " +
        		"due to bad data");
    }
    
    private PermissionSet getDefaultPermissions() {
        PermissionSet permissions = new PermissionSet();  
        Permission perm1 = new Permission();
        perm1.setCertificate(positiveCert.getBytes());
        perm1.getOperationPermission().add(OperationPermission.GET_FILE);
        Permission perm2 = new Permission();
        perm2.setCertificate(negativeCert.getBytes());
        permissions.getPermission().add(perm1);
        permissions.getPermission().add(perm2);

        return permissions;
    }
}
