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

import org.bouncycastle.asn1.x500.X500Name;

import javax.security.auth.x500.X500Principal;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Identifies a certificate by issuer (X500Principal) and serial number.
 * Those combined provide a unique ID extractable from a signature.
 */
public record CertificateID(X500Principal issuer, BigInteger serial) {

    /**
     * Creates a CertificateID from an X500Name issuer, converting it to X500Principal.
     */
    public static CertificateID of(X500Name issuer, BigInteger serialNumber) {
        try {
            return new CertificateID(new X500Principal(issuer.getEncoded()), serialNumber);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create X500Principal from X500Name", e);
        }
    }
}
