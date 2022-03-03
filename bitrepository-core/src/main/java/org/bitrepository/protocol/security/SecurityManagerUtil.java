package org.bitrepository.protocol.security;

import java.nio.file.Path;

import org.bitrepository.common.settings.Settings;

/**
 * Utility class for abstracting away the work of creating a SecurityManager
 */
public class SecurityManagerUtil {
    
    /**
     * Shorthand method to obtain a security manager, componentID is derived from settings. 
     * @param settings The settings to create the security manager for
     * @param componentCertificate Path to the certificate for the component which should use the SecurityManager
     * @return {@link SecurityManager} The security manager
     */
    public static SecurityManager getSecurityManager(Settings settings, Path componentCertificate) {
        return getSecurityManager(settings, componentCertificate, settings.getComponentID());
    }
    
    /**
     * Shorthand method to obtain a security manager
     * @param settings The settings to create the security manager for
     * @param componentCertificate Path to the certificate for the component which should use the SecurityManager
     * @param componentID The ID of the component
     * @return {@link SecurityManager} The security manager
     */
    public static SecurityManager getSecurityManager(Settings settings, Path componentCertificate, String componentID) {
        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizer authorizer = new BasicOperationAuthorizer(permissionStore);
        return new BasicSecurityManager(settings.getRepositorySettings(), componentCertificate.toString(),
                authenticator, signer, authorizer, permissionStore, componentID);
    }
}
