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

import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CertificateIDTest extends ExtendedTestCase {

    @Test(groups = {"regressiontest"})
    public void positiveCertificateIdentificationTest() throws Exception {
        addDescription("Tests that a certificate can be identified based on the correct signature.");
        addStep("Create CertificateID object based on the certificate used to sign the data", "CertificateID object not null");
        Security.addProvider(new BouncyCastleProvider());

        ByteArrayInputStream bs = new ByteArrayInputStream(
                SecurityTestConstants.getPositiveCertificate().getBytes(SecurityModuleConstants.defaultEncodingType));
        X509Certificate myCertificate = (X509Certificate) CertificateFactory.getInstance(
                SecurityModuleConstants.CertificateType).generateCertificate(bs);
        CertificateID certificateIDfromCertificate =
                new CertificateID(myCertificate.getIssuerX500Principal(), myCertificate.getSerialNumber());

        addStep("Create CertificateID object based on signature", "Certificate object not null");
        byte[] decodeSig = Base64.decode(SecurityTestConstants.getSignature().getBytes(StandardCharsets.UTF_8));
        CMSSignedData s = new CMSSignedData(new CMSProcessableByteArray(
                SecurityTestConstants.getTestData().getBytes(SecurityModuleConstants.defaultEncodingType)), decodeSig);
        SignerInformation signer = s.getSignerInfos().getSigners().iterator().next();
        CertificateID certificateIDFromSignature = new CertificateID(signer.getSID().getIssuer(), signer.getSID().getSerialNumber());

        addStep("Assert that the two CertificateID objects are equal", "Assert succeeds");
        Assert.assertEquals(certificateIDfromCertificate, certificateIDFromSignature);
    }

    @Test(groups = {"regressiontest"})
    public void negativeCertificateIdentificationTest() throws Exception {
        addDescription("Tests that a certificate is not identified based on a incorrect signature.");
        addStep("Create CertificateID object based on a certificate not used for signing the data", "CertificateID object not null");
        Security.addProvider(new BouncyCastleProvider());

        ByteArrayInputStream bs = new ByteArrayInputStream(
                SecurityTestConstants.getNegativeCertificate().getBytes(SecurityModuleConstants.defaultEncodingType));
        X509Certificate myCertificate = (X509Certificate) CertificateFactory.getInstance(
                SecurityModuleConstants.CertificateType).generateCertificate(bs);
        CertificateID certificateIDFromCertificate =
                new CertificateID(myCertificate.getIssuerX500Principal(), myCertificate.getSerialNumber());

        addStep("Create CertificateID object based on signature", "Certificate object not null");
        byte[] decodeSig = Base64.decode(SecurityTestConstants.getSignature().getBytes(StandardCharsets.UTF_8));
        CMSSignedData s = new CMSSignedData(new CMSProcessableByteArray(
                SecurityTestConstants.getTestData().getBytes(SecurityModuleConstants.defaultEncodingType)), decodeSig);
        SignerInformation signer = s.getSignerInfos().getSigners().iterator().next();
        CertificateID certificateIDFromSignature = new CertificateID(signer.getSID().getIssuer(), signer.getSID().getSerialNumber());

        addStep("Assert that the two CertificateID objects are equal", "Assert succeeds");
        Assert.assertNotSame(certificateIDFromCertificate, certificateIDFromSignature);
    }

    @Test(groups = {"regressiontest"})
    public void equalTest() throws Exception {
        addDescription("Tests the equality of CertificateIDs");
        addStep("Setup", "");
        Security.addProvider(new BouncyCastleProvider());

        ByteArrayInputStream bs = new ByteArrayInputStream(
                SecurityTestConstants.getNegativeCertificate().getBytes(SecurityModuleConstants.defaultEncodingType));
        X509Certificate myCertificate = (X509Certificate) CertificateFactory.getInstance(
                SecurityModuleConstants.CertificateType).generateCertificate(bs);
        X500Principal issuer = myCertificate.getIssuerX500Principal();
        BigInteger serial = myCertificate.getSerialNumber();
        CertificateID certificateID1 = new CertificateID(issuer, serial);

        addStep("Validate the content of the certificateID", "Should be same as x509Certificate");
        Assert.assertEquals(certificateID1.getIssuer(), issuer);
        Assert.assertEquals(certificateID1.getSerial(), serial);

        addStep("Test whether it equals it self", "should give positive result");
        Assert.assertEquals(certificateID1, certificateID1);

        addStep("Test with a null as argument", "Should give negative result");
        Assert.assertNotEquals(certificateID1, null);

        addStep("Test with another class", "Should give negative result");
        Assert.assertNotEquals(new Object(), certificateID1);

        addStep("Test with same issuer but no serial", "Should give negative result");
        Assert.assertNotEquals(new CertificateID(issuer, null), certificateID1);

        addStep("Test with same serial but no issuer", "Should give negative result");
        Assert.assertNotEquals(new CertificateID((X500Principal) null, serial), certificateID1);

        addStep("Test the positive case, with both the issuer and serial ", "Should give positive result");
        Assert.assertEquals(new CertificateID(issuer, serial), certificateID1);

        addStep("Setup an empty certificate", "");
        CertificateID certificateID2 = new CertificateID((X500Principal) null, null);

        addStep("Test empty certificate against issuer but no serial", "Should give negative result");
        Assert.assertNotEquals(new CertificateID(issuer, null), certificateID2);

        addStep("Test empty certificate against serial but no issuer", "Should give negative result");
        Assert.assertNotEquals(new CertificateID((X500Principal) null, serial), certificateID2);

        addStep("Test empty certificate against serial and issuer", "Should give negative result");
        Assert.assertNotEquals(new CertificateID(issuer, serial), certificateID2);

        addStep("Test the positive case, with neither issuer nor serial", "Should give positive result");
        Assert.assertEquals(new CertificateID((X500Principal) null, null), certificateID2);

        addStep("Check the hash codes for the two certificate", "Should not be the same");
        Assert.assertTrue(certificateID1.hashCode() != certificateID2.hashCode());
    }
}
