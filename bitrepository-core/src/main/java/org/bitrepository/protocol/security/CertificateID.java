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

import java.io.IOException;
import java.math.BigInteger;

import javax.security.auth.x500.X500Principal;
import org.bouncycastle.asn1.x500.X500Name;

/**
 * Class to be used as an identifier of certificates. 
 * Identification is based on the issuer (X500Principal) and the certificates serial number. 
 * Those combined should provide a unique ID, and the information can be extracted from a signature.      
 */
public class CertificateID {

    /** Identifying object of the issuer of a certificate */
    private final X500Principal issuer;
    /** The serial number of a certificate (unique within an issuer) */
    private final BigInteger serial;

    /**
     * Constructor for the class.
     * @param issuer The X500Principal object that identifies the certificate issuer.
     * 				 Can be extracted from a SignerID and a X509Certificate
     * @param serialNumber The certificates SerialNumber, ca be extracted from a SignerID and a X509Certificate
     */
    public CertificateID(X500Principal issuer, BigInteger serialNumber) {
        this.issuer = issuer;
        this.serial = serialNumber;
    }

    public CertificateID(X500Name issuer, BigInteger serialNumber) {
        try {
            this.issuer = new X500Principal(issuer.getEncoded());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create X500Principal from X500Name", e);
        }
        this.serial = serialNumber;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((issuer == null) ? 0 : issuer.hashCode());
        result = prime * result + ((serial == null) ? 0 : serial.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CertificateID other = (CertificateID) obj;
        if (issuer == null) {
            if (other.issuer != null)
                return false;
        } else if (!issuer.equals(other.issuer))
            return false;
        if (serial == null) {
            if (other.serial != null)
                return false;
        } else if (!serial.equals(other.serial))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CertificateID [issuer=" + issuer + ", serial=" + serial + "]";
    }

    /**
     * @see CertificateID constructor 
     */
    public X500Principal getIssuer() {
        return issuer;
    }

    /**
     * @see CertificateID constructor 
     */
    public BigInteger getSerial() {
        return serial;
    }

}
