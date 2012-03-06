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

