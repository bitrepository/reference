package org.bitrepository.protocol.security;

import java.security.KeyStore.ProtectionParameter;
import java.security.SecureRandom;

public class SecurityModuleConstants {

    /** Constant for selecting the BouncyCastle security provider */
    public final static String BC = "BC";
    /** Type of signature used for message signing */
    public final static String SignatureType = "SHA512withRSA";
    /** The certificate type used */
    public final static String CertificateType = "X.509";
    /** The default encoding type for encoding/decoding signatures and certificate data */
    public final static String defaultEncodingType = "UTF-8";
    /** The keystore implementation used. */
    public final static String keyStoreType = "BKS";
    /** Keystore alias for the private key entry */
    public final static String privateKeyAlias = "PrivateKey";
    /** Default protection parameter for 'normal' keystore entries */
    public final static ProtectionParameter nullProtectionParameter = null;
    /** The default Key- and TrustStore algorithm */
    public final static String keyTrustStoreAlgorithm = "SunX509";
    /** The default SSL protocol type*/
    public final static String defaultSSLProtocol = "TLS";
    /** The default SecureRandom for SSLContext generation */
    public final static SecureRandom defaultRandom = null;
}

