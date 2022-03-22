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

import java.security.KeyStore.ProtectionParameter;
import java.security.SecureRandom;

public class SecurityModuleConstants {

    private SecurityModuleConstants() {}

    /**
     * Constant for selecting the BouncyCastle security provider
     */
    public static final String BC = "BC";
    /**
     * Type of signature used for message signing
     */
    public static final String SignatureType = "SHA512withRSA";
    /**
     * The certificate type used
     */
    public static final String CertificateType = "X.509";
    /**
     * The default encoding type for encoding/decoding signatures and certificate data
     */
    public static final String defaultEncodingType = "UTF-8";
    /**
     * The keystore implementation used.
     */
    public static final String keyStoreType = "BKS";
    /**
     * Keystore alias for the private key entry
     */
    public static final String privateKeyAlias = "PrivateKey";
    /**
     * Default protection parameter for 'normal' keystore entries
     */
    public static final ProtectionParameter nullProtectionParameter = null;
    /**
     * The default Key- and TrustStore algorithm
     */
    public static final String keyTrustStoreAlgorithm = "SunX509";
    /**
     * The default SSL org.bitrepository.org.bitrepository.protocol type
     */
    public static final String defaultSSLProtocol = "TLS";
    /**
     * The default SecureRandom for SSLContext generation
     */
    public static final SecureRandom defaultRandom = null;
}

