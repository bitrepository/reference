package org.bitrepository.protocol.security;

import java.math.BigInteger;
import javax.security.auth.x500.X500Principal;

/**
 * Class to be used as an identifier of certificates. 
 * Identification is based on the issuer (X500Principal) and the certificates serial number. 
 * Those combined should provide a unique ID, and the information can be extracted from a signature.      
 */
public class CertificateID {

    private final X500Principal issuer;
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
