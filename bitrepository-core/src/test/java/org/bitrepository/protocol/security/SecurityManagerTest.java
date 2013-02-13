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

import java.io.UnsupportedEncodingException;

import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.protocol.security.exception.CertificateUseException;
import org.bitrepository.protocol.security.exception.MessageAuthenticationException;
import org.bitrepository.protocol.security.exception.MessageSigningException;
import org.bitrepository.protocol.security.exception.OperationAuthorizationException;
import org.bitrepository.settings.repositorysettings.Certificate;
import org.bitrepository.settings.repositorysettings.ComponentIDs;
import org.bitrepository.settings.repositorysettings.Operation;
import org.bitrepository.settings.repositorysettings.OperationPermission;
import org.bitrepository.settings.repositorysettings.Permission;
import org.bitrepository.settings.repositorysettings.PermissionSet;
import org.bouncycastle.util.encoders.Base64;
import org.jaccept.structure.ExtendedTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SecurityManagerTest extends ExtendedTestCase  {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private org.bitrepository.protocol.security.SecurityManager securityManager;
    private PermissionStore permissionStore;
    
    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
        MessageSigner messageSigner = new BasicMessageSigner();
        Settings settings = TestSettingsProvider.reloadSettings(getClass().getSimpleName());
        settings.getRepositorySettings().getProtocolSettings().setRequireMessageAuthentication(true);
        settings.getRepositorySettings().getProtocolSettings().setRequireOperationAuthorization(true);
        settings.getRepositorySettings().setPermissionSet(SecurityTestConstants.getDefaultPermissions());
        securityManager = new BasicSecurityManager(settings.getRepositorySettings(),
                        SecurityTestConstants.getKeyFile(), 
                        authenticator, 
                        messageSigner, 
                        authorizer, 
                        permissionStore,
                        SecurityTestConstants.getComponentID());
    }
    
    @Test(groups = {"regressiontest"})
    public void operationAuthorizationBehaviourTest() throws Exception {
        addDescription("Tests that a signature only allows the correct requests.");
        addStep("Check that GET_FILE is allowed.", "GET_FILE is allowed.");

        try {
            securityManager.authorizeOperation(GetFileRequest.class.getSimpleName(), 
                    SecurityTestConstants.getTestData(), SecurityTestConstants.getSignature());
        } catch (OperationAuthorizationException e) {
            Assert.fail(e.getMessage());
        }
               
        try {
            securityManager.authorizeOperation(PutFileRequest.class.getSimpleName(), 
                    SecurityTestConstants.getTestData(), SecurityTestConstants.getSignature());
            Assert.fail("SecurityManager did not throw the expected OperationAuthorizationException");
        } catch (OperationAuthorizationException e) {
            
        }  
    }        

    @Test(groups = {"regressiontest"})
    public void certificateAuthorizationBehaviourTest() throws Exception {
        addDescription("Tests that a certificate is only allowed by registered users (component).");
        addStep("Check that the registered component is allowed.", "The registered component is allowed.");

        permissionStore.loadPermissions(getSigningCertPermission(), SecurityTestConstants.getComponentID());

        try {
            securityManager.authorizeCertificateUse(SecurityTestConstants.getAllowedCertificateUser(), 
                    SecurityTestConstants.getTestData(), SecurityTestConstants.getSignature());
        } catch (CertificateUseException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertNotNull(getSigningCertPermission().getPermission().get(0).getCertificate().getAllowedCertificateUsers());
        addStep("Check that an unregistered component is not allowed.", "The unregistered component is not allowed.");
        try {
            securityManager.authorizeCertificateUse(SecurityTestConstants.getDisallowedCertificateUser(), 
                    SecurityTestConstants.getTestData(), SecurityTestConstants.getSignature());
            Assert.fail("SecurityManager did not throw the expected CertificateUseException");
        } catch (CertificateUseException e) {
            
        }  
    }        

    
    @Test(groups = {"regressiontest"})
    public void positiveSigningAuthenticationRoundtripTest() throws Exception {
        addDescription("Tests that a roundtrip of signing a request and afterwards authenticating is succedes.");
        addStep("Sign a chunck of data.", "Data is signed succesfully");
        String signature = null;
        try {
            signature = securityManager.signMessage(SecurityTestConstants.getTestData());
        } catch (MessageSigningException e) {
            Assert.fail("Failed signing test data!", e);
        }
        permissionStore.loadPermissions(getSigningCertPermission(), SecurityTestConstants.getComponentID());
        
        String signatureString = new String(Base64.encode(signature.getBytes(SecurityModuleConstants.defaultEncodingType)));
        log.info("Signature for testdata is: " + signatureString);
        
        addStep("Check signature matches the data ", "Signature and data matches");
        try {
            securityManager.authenticateMessage(SecurityTestConstants.getTestData(), signature);
        } catch (MessageAuthenticationException e) {
           Assert.fail("Failed authenticating test data!", e);
        }  
    }
        
    @Test(groups = {"regressiontest"})
    public void negativeSigningAuthenticationRoundtripUnkonwnCertificateTest() throws Exception {
        addDescription("Tests that a roundtrip of signing a request and afterwards authenticating it fails due to " +
        		"a unknown certificate.");
        addStep("Sign a chunck of data.", "Data is signed succesfully");
        String signature = null;
        try {
            signature = securityManager.signMessage(SecurityTestConstants.getTestData());
        } catch (MessageSigningException e) {
            Assert.fail("Failed signing test data!", e);
        }
        String signatureString = new String(Base64.encode(signature.getBytes(SecurityModuleConstants.defaultEncodingType)));
        log.info("Signature for testdata is: " + signatureString);
        
        addStep("Check signature matches the data", "Signature cant be matched as certificate is unknown.");
        try {
            securityManager.authenticateMessage(SecurityTestConstants.getTestData(), signature);//signatureString);
            Assert.fail("Authentication did not fail as expected");
        } catch (MessageAuthenticationException e) {
            log.info(e.getMessage());
        }  
    }   
    
    @Test(groups = {"regressiontest"})
    public void negativeSigningAuthenticationRoundtripBadDataTest() throws Exception {
        addDescription("Tests that a roundtrip of signing a request and afterwards authenticating it fails " +
        		"due to bad data");
        addDescription("Tests that a roundtrip of signing a request and afterwards authenticating is succedes.");
        addStep("Sign a chunck of data.", "Data is signed succesfully");
        String signature = null;
        try {
            signature = securityManager.signMessage(SecurityTestConstants.getTestData());
        } catch (MessageSigningException e) {
            Assert.fail("Failed signing test data!", e);
        }
        permissionStore.loadPermissions(getSigningCertPermission(), SecurityTestConstants.getComponentID());
        
        String signatureString = new String(Base64.encode(signature.getBytes(SecurityModuleConstants.defaultEncodingType)));
        log.info("Signature for testdata is: " + signatureString);
        
        addStep("Check signature matches the data ", "Signature and data matches does not match");
        String corruptData = SecurityTestConstants.getTestData() + "foobar";
        try {
            securityManager.authenticateMessage(corruptData, signature);
            Assert.fail("Authentication did not fail as expected!");
        } catch (MessageAuthenticationException e) {
            log.info(e.getMessage());
        }  
    }
    
    private PermissionSet getSigningCertPermission() throws UnsupportedEncodingException {
        PermissionSet permissions = new PermissionSet();
        ComponentIDs allowedUsers = new ComponentIDs();
        allowedUsers.getIDs().add(SecurityTestConstants.getAllowedCertificateUser());
        Permission signingCertPerm = new Permission();
        
        Certificate signingCert = new Certificate();
        signingCert.setCertificateData(SecurityTestConstants.getSigningCertificate()
                .getBytes(SecurityModuleConstants.defaultEncodingType));
        signingCert.setAllowedCertificateUsers(allowedUsers);
        
        signingCertPerm.setCertificate(signingCert);
        OperationPermission opPerm = new OperationPermission();
        opPerm.setOperation(Operation.ALL);
        signingCertPerm.getOperationPermission().add(opPerm);   
        permissions.getPermission().add(signingCertPerm); 
        return permissions;
    }
}
