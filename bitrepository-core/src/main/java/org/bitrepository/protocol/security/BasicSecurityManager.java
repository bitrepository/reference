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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.protocol.security.exception.CertificateUseException;
import org.bitrepository.protocol.security.exception.MessageAuthenticationException;
import org.bitrepository.protocol.security.exception.MessageSigningException;
import org.bitrepository.protocol.security.exception.OperationAuthorizationException;
import org.bitrepository.protocol.security.exception.SecurityException;
import org.bitrepository.protocol.security.exception.UnregisteredPermissionException;
import org.bitrepository.settings.repositorysettings.InfrastructurePermission;
import org.bitrepository.settings.repositorysettings.Permission;
import org.bitrepository.settings.repositorysettings.PermissionSet;
import org.bitrepository.settings.repositorysettings.RepositorySettings;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle:
 * - loading of certificates
 * - setup of SSLContext
 * - Authentication of signatures
 * - Signature generation 
 * - Authorization of operations
 */
public class BasicSecurityManager implements SecurityManager {
	/** Key to environment variable for default truststore */
	private static final String DEFAULT_TRUSTSTORE_PARAM="javax.net.ssl.trustStore";
	/** Key to environment variable for default truststore password */
	private static final String DEFAULT_TRUSTSTORE_PASS_PARAM="javax.net.ssl.trustStorePassword";
	
    private final Logger log = LoggerFactory.getLogger(BasicSecurityManager.class);
    /** Default password for the in-memory keystore */
    private static final String defaultPassword = "123456";
    /** path to file containing the components private key and certificate */
    private final String privateKeyFile;
    /** RepositorySettings */
    private final RepositorySettings repositorySettings;
    /** Object to authenticate messages */
    private final MessageAuthenticator authenticator;
    /** Object to sign messages */
    private final MessageSigner signer;
    /** Object to authorize operations */
    private final OperationAuthorizor authorizer;
    /** Object storing permissions and certificates */
    private final PermissionStore permissionStore;
    /** int value to keep track of the next keystore alias */
    private static int aliasID = 0;
    /** In memory keyStore */
    private KeyStore keyStore; 
    /** Member for holding the PrivateKeyEntry containing the from privateKeyFile loaded key and certificate */
    private PrivateKeyEntry privateKeyEntry;
    /** The ID of the component where this instance of the BasicSecurityManager is running */
    private final String componentID;
    
    /**
     * Constructor for the SecurityManager.
     * @param repositorySettings the collection settings to retrieve settings from
     * @param privateKeyFile path to the file containing the components private key and certificate, may be null if not using
     *        certificates and encryption.
     * @param authenticator MessageAuthenticator for authenticating messages
     * @param signer MessageSigner for signing messages.
     * @param authorizer OperationAuthorizer to authorize operations
     * @param permissionStore the PermissionStore to hold certificates and adjoining permissions
     * @param componentID the component ID
     */
    public BasicSecurityManager(RepositorySettings repositorySettings, String privateKeyFile, MessageAuthenticator authenticator,
            MessageSigner signer, OperationAuthorizor authorizer, PermissionStore permissionStore, String componentID) {
        ArgumentValidator.checkNotNull(repositorySettings, "repositorySettings");
        ArgumentValidator.checkNotNull(authenticator, "authenticator");
        ArgumentValidator.checkNotNull(signer, "signer");
        ArgumentValidator.checkNotNull(authorizer, "authorizer");
        ArgumentValidator.checkNotNull(permissionStore, "permissionStore");
        this.privateKeyFile = privateKeyFile;
        this.repositorySettings = repositorySettings;
        this.authenticator = authenticator;
        this.signer = signer;
        this.authorizer = authorizer;
        this.permissionStore = permissionStore;
        this.componentID = componentID;
        initialize();
    }
    
    @Override
    public SignerId authenticateMessage(String message, String signature) throws MessageAuthenticationException {
        if(repositorySettings.getProtocolSettings().isRequireMessageAuthentication()) {
            if (signature != null) {
            try {
                byte[] decodedSig = Base64.decode(signature.getBytes(SecurityModuleConstants.defaultEncodingType));
                byte[] decodeMessage = message.getBytes(SecurityModuleConstants.defaultEncodingType);
                return authenticator.authenticateMessage(decodeMessage, decodedSig);
            } catch (UnsupportedEncodingException e) {
                throw new SecurityException(SecurityModuleConstants.defaultEncodingType + " encoding not supported", e);
            }
            } else {
                throw new MessageAuthenticationException("Received unsigned message, but authentication is required");
            }
        }
        return null;
    }
    
    /**
     * Method to sign a message
     * @param message the message to sign
     * @return the signature for the message, or null if authentication is disabled.
     * @throws MessageSigningException if signing of the message fails.   
     */
    public String signMessage(String message) throws MessageSigningException {
        if(repositorySettings.getProtocolSettings().isRequireMessageAuthentication()) {
            try {
                byte[] signature = signer.signMessage(message.getBytes(SecurityModuleConstants.defaultEncodingType));
                return new String(Base64.encode(signature), StandardCharsets.UTF_8);
            } catch (UnsupportedEncodingException e) {
                throw new SecurityException(SecurityModuleConstants.defaultEncodingType + " encoding not supported", e);
            }           
        } else { 
            return null;
        }
    }
    
    /** 
     * Method to authorize the use of a certificate
     * @param certificateUser the user who signed the message
     * @param messageData the data of the message request.
     * @param signature the signature belonging to the message request.
     * @throws CertificateUseException in case the certificate use could not be authorized. 
     */
    public void authorizeCertificateUse(String certificateUser, String messageData, String signature) 
            throws CertificateUseException {
        if(repositorySettings.getProtocolSettings().isRequireOperationAuthorization()) {
            byte[] decodeSig = Base64.decode(signature.getBytes(StandardCharsets.UTF_8));
            CMSSignedData s;
            try {
                s = new CMSSignedData(new CMSProcessableByteArray(messageData.getBytes(StandardCharsets.UTF_8)), decodeSig);
            } catch (CMSException e) {
                throw new SecurityException(e.getMessage(), e);
            }
    
            SignerInformation signer = (SignerInformation) s.getSignerInfos().getSigners().iterator().next();
            authorizer.authorizeCertificateUse(certificateUser, signer.getSID());    
        }
    }

    @Override
    public String getCertificateFingerprint(SignerId signer) throws UnregisteredPermissionException {
            return permissionStore.getCertificateFingerprint(signer);
    }

    /**
     * Method to authorize an operation 
     * @param operationType the type of operation that is to be authorized.
     * @param messageData the data of the message request.
     * @param signature the signature belonging to the message request.
     * @throws OperationAuthorizationException in case of failure. 
     */
    public void authorizeOperation(String operationType, String messageData, String signature, String collectionID) 
            throws OperationAuthorizationException {
        if(repositorySettings.getProtocolSettings().isRequireOperationAuthorization()) {
            byte[] decodeSig = Base64.decode(signature.getBytes(StandardCharsets.UTF_8));
            CMSSignedData s;
            try {
                s = new CMSSignedData(new CMSProcessableByteArray(messageData.getBytes(StandardCharsets.UTF_8)), decodeSig);
            } catch (CMSException e) {
                throw new SecurityException(e.getMessage(), e);
            }
    
            SignerInformation signer = (SignerInformation) s.getSignerInfos().getSigners().iterator().next();
            try {
                authorizer.authorizeOperation(operationType, signer.getSID(), collectionID);    
            } catch (UnregisteredPermissionException e) {
                log.info(e.getMessage());
            }
        }
    }
    
    /**
     * Do initialization work
     * - Creates keystore
     * - Loads private key and certificate
     * - Loads permissions and certificates
     * - Sets up SSLContext
     */
    private void initialize() {
        Security.addProvider(new BouncyCastleProvider());
        try {
            keyStore = getKeyStore();
            loadPrivateKey(privateKeyFile);
            loadInfrastructureCertificates(repositorySettings.getPermissionSet());
            permissionStore.loadPermissions(repositorySettings.getPermissionSet(), componentID);
            signer.setPrivateKeyEntry(privateKeyEntry);
            setupDefaultSSLContext();
        } catch (Exception e) {
            throw new SecurityException(e.getMessage(), e);
        } 
    }
    
    /**
     * Obtain the keystore in which to place certificates loaded by the bitrepository.
     * Attempt to load trusted certificates from a truststore specified by environment variables.
     * If a truststore is specified by environment variables, the returned KeyStore will have its trusted
     * certificates loaded. Otherwise the returned keystore will be empty.
     * This is in order to not throw out default trust store.
     * @return KeyStore, conditionally containing trusted certificates from the external truststore.
     */
    private KeyStore getKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore store = KeyStore.getInstance(SecurityModuleConstants.keyStoreType);
        store.load(null);

        KeyStore systemTrustStore = loadSystemTrustStore();
        if(systemTrustStore != null) {
            Enumeration<String> systemAliases = systemTrustStore.aliases();
            while(systemAliases.hasMoreElements()) {
                String alias = systemAliases.nextElement();
                Certificate certificate = systemTrustStore.getCertificate(alias);
                store.setEntry(getNewAlias(), new KeyStore.TrustedCertificateEntry(certificate),
                        SecurityModuleConstants.nullProtectionParameter);
            }
        }
        return store;
    }

    /**
     * Load the truststore specified by environment variables, if specified
     * @return KeyStore representing the truststore provided by environment variable. If no truststore is specified
     * by environment variables null is returned.
     */
    private KeyStore loadSystemTrustStore() throws KeyStoreException, IOException, NoSuchAlgorithmException,
                CertificateException {
        KeyStore store = null;
        String defaultTrustStoreLocation = System.getProperty(DEFAULT_TRUSTSTORE_PARAM);
        if(defaultTrustStoreLocation != null) {
            File defaultTrustStore = new File(defaultTrustStoreLocation);
            if(defaultTrustStore.isFile() && defaultTrustStore.canRead()) {
                store = KeyStore.getInstance(KeyStore.getDefaultType());
                String trustStorePassword = System.getProperty(DEFAULT_TRUSTSTORE_PASS_PARAM);
                try (FileInputStream fis = new FileInputStream(defaultTrustStore)) {
                    store.load(fis, trustStorePassword.toCharArray());
                }
            }
        }

        return store;
    }

    /**
     * Alias generator for the keystore entries.
     * @return returns a String containing the alias for the next keystore entry 
     */
    private String getNewAlias() {
        return "" + aliasID++;
    }
    
    /**
     * Attempts to load the pillars private key and certificate from a PEM formatted file. 
     * @param privateKeyFile path to the file containing the components private key and certificate, may be null
     * @throws IOException if the file cannot be found or read. 
     * @throws KeyStoreException if there is problems with adding the privateKeyEntry to keyStore
     * @throws CertificateException 
     */
    private void loadPrivateKey(String privateKeyFile) throws IOException, KeyStoreException, 
            CertificateException {
        PrivateKey privKey = null;
        X509Certificate privCert = null;
        if(privateKeyFile == null || !(new File(privateKeyFile)).isFile()) {
            log.info("Key file '" + privateKeyFile + "' with private key and certificate does not exist!");
            return;
        }
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(privateKeyFile), StandardCharsets.UTF_8));
        PEMParser pemParser = new PEMParser(bufferedReader);
        Object pemObj = pemParser.readObject();

        while(pemObj != null) {
            if(pemObj instanceof X509Certificate) {
                log.debug("Certificate for PrivateKeyEntry found");
                privCert = (X509Certificate) pemObj;
            } else if(pemObj instanceof PrivateKey) {
                log.debug("Key for PrivateKeyEntry found");
                privKey = (PrivateKey) pemObj;
            } else if(pemObj instanceof X509CertificateHolder ) {
                log.debug("X509CertificateHolder found");
                privCert = new JcaX509CertificateConverter().setProvider("BC")
                        .getCertificate((X509CertificateHolder) pemObj);
            } else if(pemObj instanceof PrivateKeyInfo) {
                log.debug("PrivateKeyInfo found");
                PrivateKeyInfo pki = (PrivateKeyInfo) pemObj;
                JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                privKey = converter.getPrivateKey(pki);
            } else {
                log.debug("Got something, that we don't (yet) recognize. Class: " + pemObj.getClass().getSimpleName());
            }
            pemObj = pemParser.readObject();
        }
        
        pemParser.close();
        if(privKey == null || privCert == null ) {
            log.info("No material to create private key entry found!");
        } else {
            privCert.checkValidity();
            privateKeyEntry = new PrivateKeyEntry(privKey, new Certificate[] {privCert});
            keyStore.setEntry(SecurityModuleConstants.privateKeyAlias, privateKeyEntry, 
                    new KeyStore.PasswordProtection(defaultPassword.toCharArray()));
        }
    }

    /**
     * Load the appropriate certificates from PermissionSet into trust/keystore
     * @param permissions the permission set
     * @throws CertificateException if certificate cannot be created from the data
     * @throws KeyStoreException if certificate cannot be put into the keyStore
     */
    private void loadInfrastructureCertificates(PermissionSet permissions) throws CertificateException, KeyStoreException {
        if(permissions == null) {
            log.info("The provided PermissionSet is empty. Continuing without permissions!");
            return;
        }
        for(Permission permission : permissions.getPermission()) {
            if(permission.getInfrastructurePermission().contains(InfrastructurePermission.MESSAGE_BUS_SERVER) 
                    || permission.getInfrastructurePermission().contains(InfrastructurePermission.FILE_EXCHANGE_SERVER)) {
	            try (ByteArrayInputStream bs = new ByteArrayInputStream(permission.getCertificate().getCertificateData())) {
	                X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance(
	                        SecurityModuleConstants.CertificateType).generateCertificate(bs);
	                certificate.checkValidity();
	                keyStore.setEntry(getNewAlias(), new KeyStore.TrustedCertificateEntry(certificate), 
	                        SecurityModuleConstants.nullProtectionParameter);
	            } catch (CertificateException ce) {
	                log.warn("Check of certificate validity failed, not adding certificate ({}) to keystore.", 
	                        permission.getDescription(), ce);
	            } catch (IOException e) {
	                log.debug("Failed closing ByteArrayInputStream", e);
	            }
            }
        }      
    }
    
    /**
     * Sets up the Default SSL context  
     * @throws NoSuchAlgorithmException if the TrustStoreFactory does now know the TrustStoreAlgorithm
     * @throws KeyStoreException if the TrustManagerFactory cannot be init'ed
     * @throws UnrecoverableKeyException If the password is wrong
     * @throws KeyManagementException if the SSL Context cannot be init'ed
     */
    private void setupDefaultSSLContext() throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
        TrustManagerFactory tmf;
        KeyManagerFactory kmf;
        SSLContext context;
        tmf = TrustManagerFactory.getInstance(SecurityModuleConstants.keyTrustStoreAlgorithm);
        tmf.init(keyStore);
        kmf = KeyManagerFactory.getInstance(SecurityModuleConstants.keyTrustStoreAlgorithm);
        kmf.init(keyStore, defaultPassword.toCharArray());
        context = SSLContext.getInstance(SecurityModuleConstants.defaultSSLProtocol);
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), SecurityModuleConstants.defaultRandom);
        SSLContext.setDefault(context);
    }
}
