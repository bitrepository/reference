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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bitrepository.settings.collectionsettings.InfrastructurePermission;
import org.bitrepository.settings.collectionsettings.OperationPermission;
import org.bitrepository.settings.collectionsettings.PermissionSet;
import org.bitrepository.settings.collectionsettings.Permission;
import org.bouncycastle.cms.SignerId;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to hold the concept of permissions used in the Bitrepository.
 * The class contains functionality to:
 * - Hold the correlation between certificates and the permissions related to them.
 * - Test if a certificate has a requested permission
 * - Retreive a certificate from the store.
 */
public class PermissionStore {

    private final Logger log = LoggerFactory.getLogger(PermissionStore.class);
    /** Mapping from certificate identifier to an object containing the certificate and the permissions registered with it*/
    private Map<CertificateID, CertificatePermission> permissionMap;

    /**
     * Public constructor, initializes the store. 
     */
    public PermissionStore() {
        permissionMap = new HashMap<CertificateID, CertificatePermission>();
        Provider provider = new BouncyCastleProvider();
        Security.addProvider(provider);
    }
    
    /**
     * Load permissions and certificates into the store based.
     * @param PermissionSet the PermissionSet from CollectionSettings.
     * @throws CertificateException in case a bad certificate data in PermissionSet.   
     */
    public void loadPermissions(PermissionSet permissions) throws CertificateException {
        if(permissions != null) {
            for(Permission permission : permissions.getPermission()) {
                if(permission.getOperationPermission() != null) {
                    ByteArrayInputStream bs = new ByteArrayInputStream(permission.getCertificate());
                    X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance(
                            SecurityModuleConstants.CertificateType).generateCertificate(bs);
                    CertificateID certID = new CertificateID(certificate.getIssuerX500Principal(), certificate.getSerialNumber());
                    CertificatePermission certificatePermission = new CertificatePermission(certificate, 
                            permission.getOperationPermission());
                    permissionMap.put(certID, certificatePermission);
                    try {
                        bs.close();
                    } catch (IOException e) {
                        log.debug("Failed to close ByteArrayInputStream", e);
                    }
                } else if(permission.getInfrastructurePermission().contains(InfrastructurePermission.MESSAGE_SIGNER)) {
                    ByteArrayInputStream bs = new ByteArrayInputStream(permission.getCertificate());
                    X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance(
                            SecurityModuleConstants.CertificateType).generateCertificate(bs);
                    CertificateID certID = new CertificateID(certificate.getIssuerX500Principal(), certificate.getSerialNumber());
                    CertificatePermission certificatePermission = new CertificatePermission(certificate, 
                            new HashSet<OperationPermission>());
                    permissionMap.put(certID, certificatePermission);
                    try {
                        bs.close();
                    } catch (IOException e) {
                        log.debug("Failed to close ByteArrayInputStream", e);
                    }
                } else {
                    // Noting to do here
                }
            }
        } else {
            log.info("The provided PermissionSet was null");
        }
    }
    
    /**
     * Retrieve the certificate based on the signerId.
     * @param SignerId the identification data of the certificate to retrieve  
     * @return X509Certificate the certificate represented by the SignerId
     * @throws PermissionStoreException if no certificate can be found based on the SignerId 
     */
    public X509Certificate getCertificate(SignerId signer) throws PermissionStoreException {
        CertificateID certificateID = new CertificateID(signer.getIssuer(), signer.getSerialNumber());
        CertificatePermission permission = permissionMap.get(certificateID);
        if(permission != null) {
            return permission.getCertificate();
        } else {
            throw new PermissionStoreException("Failed to find certificate for the requested signer:" + certificateID.toString());
        }
    }
    
    /**
     * Check to see if a certificate has the specified permission. The certificate is identified based 
     * on the SignerId of the signature. 
     * @return true if the requested permission is present for the certificate belonging to the signer, otherwise false.
     * @throws PermissionStoreException in case no certificate and permission set can be found for the provided signer.
     */
    public boolean checkPermission(SignerId signer, OperationPermission permission) throws PermissionStoreException {
        CertificateID certificateID = new CertificateID(signer.getIssuer(), signer.getSerialNumber());
        CertificatePermission certificatePermission = permissionMap.get(certificateID);
        if(certificatePermission == null) {
            throw new PermissionStoreException("Failed to find certificate and permissions for the requested signer: " +
                    certificateID.toString());
        } else {
            return certificatePermission.hasPermission(permission);
        }
    }
    
    /**
     * Class to contain a X509Certificate and the permissions associated with it.    
     */
    private final class CertificatePermission {
        private Set<OperationPermission> permissions;
        private final X509Certificate certificate;
            
        /**
         * Constructor
         * @param certificate, the certificate which permissions is to be represented.
         * @param permissions, the permissions related to the certificate. 
         */
        public CertificatePermission(X509Certificate certificate, Collection<OperationPermission> permissions) {
            this.permissions = new HashSet<OperationPermission>();
            this.permissions.addAll(permissions);
            this.certificate = certificate;
        }
        
        /**
         *  Test if a certain permission has been registered for this object. 
         *  @param permission, the permission to test for
         *  @return true if the permission is registered, false otherwise.
         */
        public boolean hasPermission(OperationPermission permission) {
            return permissions.contains(permission);
        }
        /** 
         * Retrieve the certificate from the object. 
         * @return the X509Certificate from the object.
         */
        public final X509Certificate getCertificate() {
            return certificate;
        }   
    }

}
