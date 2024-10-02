package org.bitrepository.protocol.security;

import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.encoders.Base64;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * Simple class for loading test certificates and simplifying actions related to them.
 * Previously the certificates were hardcoded, which did not seem very practical once they expired.
 * With this class it should only be necessary to update the cert/key-files once they expire and that's it.
 */
public class TestCertProvider {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static X509Certificate loadPositiveCert() throws Exception {
        return loadCertificate("client80-certkey.pem");
    }

    public static X509Certificate loadNegativeCert() throws Exception {
        return loadCertificate("client90-certkey.pem");
    }

    public static X509Certificate loadSigningCert() throws Exception {
        return loadCertificate("client100-certkey.pem");
    }

    public static String getFingerprintForPositiveCert() throws Exception {
        X509Certificate signingCert = loadPositiveCert();
        return DigestUtils.sha1Hex(signingCert.getEncoded());
    }

    public static String getPositiveCertSignature() throws Exception {
        MessageSigner signer = new BasicMessageSigner();
        signer.setPrivateKeyEntry(loadPrivateKeyEntry("client80-certkey.pem"));
        byte[] signature = signer.signMessage(SecurityTestConstants.getTestData().getBytes(StandardCharsets.UTF_8));
        return new String(Base64.encode(signature), StandardCharsets.UTF_8);
    }

    private static X509Certificate loadCertificate(String fileName) throws Exception {
        X509Certificate cert = null;

        Path pemFilePath = Path.of(TestCertProvider.class.getClassLoader().getResource(fileName).toURI());
        try (BufferedReader reader = Files.newBufferedReader(pemFilePath, StandardCharsets.UTF_8);
             PEMParser pemParser = new PEMParser(reader)) {

            Object pemObj = pemParser.readObject();
            if (pemObj instanceof X509Certificate) {
                cert = (X509Certificate) pemObj;
            } else if (pemObj instanceof X509CertificateHolder) {
                cert = new JcaX509CertificateConverter().setProvider("BC").getCertificate((X509CertificateHolder) pemObj);
            }
        }
        return cert;
    }

    private static KeyStore.PrivateKeyEntry loadPrivateKeyEntry(String fileName) throws Exception {
        PrivateKey privKey = null;
        X509Certificate privCert = null;

        Path pemFilePath = Path.of(TestCertProvider.class.getClassLoader().getResource(fileName).toURI());
        try (BufferedReader reader = Files.newBufferedReader(pemFilePath, StandardCharsets.UTF_8);
             PEMParser pemParser = new PEMParser(reader)) {
            Object pemObj = pemParser.readObject();
            while (pemObj != null) {
                if (pemObj instanceof X509Certificate) {
                    privCert = (X509Certificate) pemObj;
                } else if (pemObj instanceof PrivateKey) {
                    privKey = (PrivateKey) pemObj;
                } else if (pemObj instanceof X509CertificateHolder) {
                    privCert = new JcaX509CertificateConverter().setProvider("BC").getCertificate((X509CertificateHolder) pemObj);
                } else if (pemObj instanceof PrivateKeyInfo) {
                    PrivateKeyInfo pki = (PrivateKeyInfo) pemObj;
                    JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
                    privKey = converter.getPrivateKey(pki);
                }
                pemObj = pemParser.readObject();
            }
        }
        return new KeyStore.PrivateKeyEntry(privKey, new Certificate[] {privCert});
    }
}
