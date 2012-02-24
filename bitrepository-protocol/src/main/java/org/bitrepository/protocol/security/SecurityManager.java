package org.bitrepository.protocol.security;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.bitrepository.settings.collectionsettings.CollectionSettings;
import org.bitrepository.settings.collectionsettings.InfrastructurePermission;
import org.bitrepository.settings.collectionsettings.Permission;
import org.bitrepository.settings.collectionsettings.PermissionSet;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
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
public class SecurityManager {
    private final Logger log = LoggerFactory.getLogger(SecurityManager.class);
    private final static String defaultPassword = "123456";
    private final String privateKeyFile;
    private final CollectionSettings collectionSettings;
    private final MessageAuthenticator authenticator;
    private final MessageSigner signer;
    private final OperationAuthorizor authorizer;
    private static int aliasID = 0;
    private KeyStore keyStore;
    private PrivateKeyEntry privateKeyEntry;
    
    /**
     * Constructor for the class. 
     */
    public SecurityManager(CollectionSettings collectionSettings, String privateKeyFile, MessageAuthenticator authenticator,
            MessageSigner signer, OperationAuthorizor authorizer) {
        this.privateKeyFile = privateKeyFile;
        this.collectionSettings = collectionSettings;
        this.authenticator = authenticator;
        this.signer = signer;
        this.authorizer = authorizer;
        initialize();
    }
    
    /**
     * Method to authenticate a message. 
     * @param String message, the message that needs to be authenticated. 
     * @param String signature, the signature belonging to the message. 
     * @throws MessageAuthenticationException in case of failure.
     */
    public void authenticateMessage(String message, String signature) throws MessageAuthenticationException {
        if(collectionSettings.getProtocolSettings().isRequireMessageAuthentication()) {
            try {
                byte[] decodedSig = Base64.decode(signature.getBytes("UTF-8"));
                byte[] decodeMessage = message.getBytes("UTF-8");
                authenticator.authenticateMessage(decodeMessage, decodedSig);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 encoding not supported");
            }
        }
    }
    
    /**
     * Method to sign a message
     * @param String message, the message to sign
     * @return String the signature for the message, or null if authentication is disabled. 
     * @throws MessageSigningException if signing of the message fails.   
     */
    public String signMessage(String message) throws MessageSigningException {
        if(collectionSettings.getProtocolSettings().isRequireMessageAuthentication()) {
            try {
                byte[] signature = signer.signMessage(message.getBytes("UTF-8"));
                return new String(Base64.encode(signature));   
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 encoding not supported");
            }           
        } else { 
            return null;
        }
    }
    
    /**
     * Method to authorize an operation 
     * @param String operationType, the type of operation that is to be authorized. 
     * @param String messageData, the data of the message request. 
     * @param String signature, the signature belonging to the message request. 
     * @throws OperationAuthorizationException in case of failure. 
     */
    public void authorizeOperation(String operationType, String messageData, String signature) 
            throws OperationAuthorizationException {
        if(collectionSettings.getProtocolSettings().isRequireOperationAuthorization()) {
            // call authorizer to authorize operation
        }
    }
    
    /**
     * Do initialization work
     */
    private void initialize() {
        Security.addProvider(new BouncyCastleProvider());
        try {
            keyStore = KeyStore.getInstance("BKS");
            keyStore.load(null);
            loadPrivateKey(privateKeyFile);
            loadInfrastructureCertificates(collectionSettings.getPermissionSet());
            setupDefaultSSLContext();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
    }
    
    /**
     * Alias generator for the keystore entries. 
     */
    private String getNewAlias() {
        return "" + aliasID++;
    }
    
    /**
     * Attempts to load the pillars private key and certificate from a PEM formatted file. 
     * @throws IOException if the file cannot be found or read. 
     * @throws KeyStoreException if there is problems with adding the privateKeyEntry to keyStore
     * @throws CertificateExpiredException if the certificate has expired 
     * @throws CertificateNotYetValidException if the certificate is not yet valid
     */
    private void loadPrivateKey(String privateKeyFile) throws IOException, KeyStoreException, 
            CertificateExpiredException, CertificateNotYetValidException {
        PrivateKey privKey = null;
        X509Certificate privCert = null;
        if(!(new File(privateKeyFile)).exists()) {
            log.info("Key file with private key and certificate does not exist!");
            return;
        }
        BufferedReader bufferedReader = new BufferedReader(new FileReader(privateKeyFile));
        PEMReader pemReader =  new PEMReader(bufferedReader);
        Object pemObj = pemReader.readObject();

        while(pemObj != null) {
            if(pemObj instanceof X509Certificate) {
                log.debug("Certificate for PrivateKeyEntry found");
                privCert = (X509Certificate) pemObj;
            } else if(pemObj instanceof PrivateKey) {
                log.debug("Key for PrivateKeyEntry found");
                privKey = (PrivateKey) pemObj;
            } else {

            }
            pemObj = pemReader.readObject();
        }
        if(privKey == null || privCert == null ) {
            log.info("No material to create private key entry found!");
        } else {
            privCert.checkValidity();
            privateKeyEntry = new PrivateKeyEntry(privKey, new Certificate[] {privCert});
            keyStore.setEntry("PrivateKey", privateKeyEntry, new KeyStore.PasswordProtection(defaultPassword.toCharArray()));
        }
    }

    /**
     * Load the appropriate certificates from PermissionSet into trust/keystore 
     * @throws CertificateException if certificate cannot be created from the data
     * @throws KeyStoreException if certificate cannot be put into the keyStore
     */
    private void loadInfrastructureCertificates(PermissionSet permissions) throws CertificateException, KeyStoreException {
        ByteArrayInputStream bs;
        for(Permission permission : permissions.getPermission()) {
            if(permission.getInfrastructurePermission().contains(InfrastructurePermission.MESSAGE_BUS_SERVER)) {
                bs = new ByteArrayInputStream(permission.getCertificate());
                X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(bs);
                keyStore.setEntry(getNewAlias(), new KeyStore.TrustedCertificateEntry(certificate), null);
            }
            if(permission.getInfrastructurePermission().contains(InfrastructurePermission.FILE_EXCHANGE_SERVER)) {
                bs = new ByteArrayInputStream(permission.getCertificate());
                X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(bs);
                keyStore.setEntry(getNewAlias(), new KeyStore.TrustedCertificateEntry(certificate), null);
            }
        }
    }
    
    /**
     * Sets up the Default SSL context  
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws UnrecoverableKeyException
     * @throws KeyManagementException
     */
    private void setupDefaultSSLContext() throws NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, 
            KeyManagementException {
        TrustManagerFactory tmf;
        KeyManagerFactory kmf;
        SSLContext context;
        tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(keyStore);
        kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(keyStore, defaultPassword.toCharArray());
        context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        SSLContext.setDefault(context);
    }
}
