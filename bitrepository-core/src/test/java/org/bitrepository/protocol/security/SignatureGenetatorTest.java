package org.bitrepository.protocol.security;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.protocol.security.exception.MessageSigningException;
import org.testng.annotations.Test;

public class SignatureGenetatorTest {

    /*
     * Test to generate new signature for SecurityTestConstants 
     */
    @Test(enabled = false)
    public void generateSignature() throws MessageSigningException {
        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
        MessageSigner messageSigner = new BasicMessageSigner();
        Settings settings = TestSettingsProvider.reloadSettings(getClass().getSimpleName());
        settings.getRepositorySettings().getProtocolSettings().setRequireMessageAuthentication(true);
        settings.getRepositorySettings().getProtocolSettings().setRequireOperationAuthorization(true);
        SecurityManager securityManager = new BasicSecurityManager(settings.getRepositorySettings(),
                SecurityTestConstants.getPositiveCertKeyFile(), 
                authenticator, 
                messageSigner, 
                authorizer, 
                permissionStore,
                SecurityTestConstants.getComponentID());
        
        String messageToSign = SecurityTestConstants.getTestData();
        String signature = securityManager.signMessage(messageToSign);
        System.out.println("messageToSign = '" + messageToSign + "', signature = '" + signature + "'");
    }
    
}
