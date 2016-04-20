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
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.bitrepository.protocol.security.exception.PermissionStoreException;
import org.bitrepository.protocol.security.exception.UnregisteredPermissionException;
import org.bitrepository.settings.repositorysettings.InfrastructurePermission;
import org.bitrepository.settings.repositorysettings.Operation;
import org.bitrepository.settings.repositorysettings.OperationPermission;
import org.bitrepository.settings.repositorysettings.Permission;
import org.bitrepository.settings.repositorysettings.PermissionSet;
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
     * @param permissions the PermissionSet from RepositorySettings.
     * @param componentID the ID of the component using the PermissionStore. 
     * @throws CertificateException in case a bad certificate data in PermissionSet.   
     */
    public void loadPermissions(PermissionSet permissions, String componentID) throws CertificateException {
        if(permissions != null) {
            Set<Operation> allowedOperations;
            Set<String> allowedUsers;
            for(Permission permission : permissions.getPermission()) {
                try {
                    if(permission.getCertificate().getAllowedCertificateUsers() != null) {
                        allowedUsers = new HashSet<String>();
                        allowedUsers.addAll(permission.getCertificate().getAllowedCertificateUsers().getIDs());
                    } else {
                        allowedUsers = null;
                    }
    
                    allowedOperations = new HashSet<Operation>();
                    X509Certificate certificate = null;
                    if(permission.getOperationPermission() != null) {
                        for(OperationPermission perm : permission.getOperationPermission()) {
                            if(perm.getAllowedComponents() == null ||
                                    perm.getAllowedComponents().getIDs().contains(componentID)) {
                                allowedOperations.add(perm.getOperation());
                            }
                        }
                        if(!allowedOperations.isEmpty()) {
                            certificate = makeCertificate(permission.getCertificate().getCertificateData());
                        }
                    }
                    if(permission.getInfrastructurePermission().contains(InfrastructurePermission.MESSAGE_SIGNER)) {
                        if(certificate == null) {
                            certificate = makeCertificate(permission.getCertificate().getCertificateData());
                        }
                    }
    
                    if(certificate != null) {
                        CertificateID certID = new CertificateID(certificate.getIssuerX500Principal(),
                                certificate.getSerialNumber());
                        CertificatePermission certificatePermission = new CertificatePermission(certificate, allowedOperations,
                                allowedUsers);
                        permissionMap.put(certID, certificatePermission);
                    }
                } catch (CertificateException ce) {
                    log.warn("Failed handle certificate with description '{}'. Certificate not added to permission store.", 
                            permission.getDescription(), ce);
                } 
            }
        } else {
            log.info("The provided PermissionSet was null");
        }
    }

    /**
     * Retrieve the certificate based on the signerId.
     * @param signer the identification data of the certificate to retrieve
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
     * @param signer the signerId of the certificate used to sign the message.
     * @param certificateUser the user that claims to have used the certificate.
     * @return true, if the certificateUser has been registered for use of the certificate indicated by signer,
     *         false otherwise.
     * @throws PermissionStoreException in case no certificate has been registered for the given signerId
     */
    public boolean checkCertificateUser(SignerId signer, String certificateUser) throws PermissionStoreException {
        CertificateID certificateID = new CertificateID(signer.getIssuer(), signer.getSerialNumber());
        CertificatePermission certificatePermission = permissionMap.get(certificateID);
        if(certificatePermission == null) {
            throw new PermissionStoreException("Failed to find certificate and permissions for the requested signer: " +
                    certificateID.toString());
        } else {
            return certificatePermission.isUserAllowed(certificateUser);
        }
    }

    /**
     * @return  the store fingerprint for the signers certificate.
     * @param signer the id of the signer
     * @throws UnregisteredPermissionException No finger print could be found for the indicated signer.
     */
    public String getCertificateFingerprint(SignerId signer) throws UnregisteredPermissionException {
        CertificateID certificateID = new CertificateID(signer.getIssuer(), signer.getSerialNumber());
        CertificatePermission certificatePermission = permissionMap.get(certificateID);
        if (certificatePermission != null) {
            return certificatePermission.getFingerprint();
        } else {
            throw new UnregisteredPermissionException("No certificate fingerprint found for signer " + signer);
        }
    }

    /**
     * Check to see if a certificate has the specified permission. The certificate is identified based 
     * on the SignerId of the signature.
     * @param signer the id of the signer
     * @param permission the operation to check if is permitted
     * @return true if the requested permission is present for the certificate belonging to the signer, otherwise false.
     * @throws PermissionStoreException in case no certificate and permission set can be found for the provided signer.
     */
    public boolean checkPermission(SignerId signer, Operation permission) throws PermissionStoreException {
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
     * Creates a X509Certificate and checks its validity
     * @param certificateData The certificate data 
     * @return {@link X509Certificate} The certificate represented by the input data
     * @throws CertificateException upon failure to parse the certificate data to a certificate 
     * or if the certificate fails the validity check.  
     */
    private X509Certificate makeCertificate(byte[] certificateData) throws CertificateException {
        ByteArrayInputStream bs = new ByteArrayInputStream(certificateData);
        X509Certificate certificate = (X509Certificate) CertificateFactory.getInstance(
                SecurityModuleConstants.CertificateType).generateCertificate(bs);
        try {
            bs.close();
        } catch (IOException e) {
            log.debug("Failed to close ByteArrayInputStream", e);
        }
        certificate.checkValidity();
        return certificate;
    }

    /**
     * Class to contain a X509Certificate and the permissions associated with it.    
     */
    private final class CertificatePermission {
        private final Set<Operation> permissions;
        private final Set<String> allowedUsers;
        private final X509Certificate certificate;
        private final String fingerprint;

        /**
         * @param certificate the certificate which permissions is to be represented.
         * @param allowedOperations the allowed operations related to the certificate.
         * @param allowedUsers the allowed users of this certificate, if users are not restricted provide null
         * @throws CertificateEncodingException if the certificate fails to be encoded
         */
        public CertificatePermission(X509Certificate certificate, Collection<Operation> allowedOperations,
                                     Collection<String> allowedUsers) throws CertificateEncodingException {
            if(allowedUsers == null) {
                this.allowedUsers = null;
            } else {
                this.allowedUsers = new HashSet<String>();
                this.allowedUsers.addAll(allowedUsers);
            }
            this.permissions = new HashSet<Operation>();
            this.certificate = certificate;
            this.permissions.addAll(allowedOperations);
            this.fingerprint = DigestUtils.sha1Hex(certificate.getEncoded());
        }

        /**
         *  Test if a certain permission has been registered for this object. 
         *  @param permission the permission to test for
         *  @return true if the permission is registered, false otherwise.
         */
        public boolean hasPermission(Operation permission) {
            return permissions.contains(permission);
        }

        /**
         *  Test if a certain certificate user has been registered as one allowed for use of this certificate.
         *  @param certificateUser the user to test for
         *  @return true if the user has been registered, false otherwise. 
         */
        public boolean isUserAllowed(String certificateUser) {
            if(allowedUsers == null) {
                return true;
            } else {
                return allowedUsers.contains(certificateUser);
            }
        }

        /**
         * Retrieve the certificate from the object. 
         * @return the X509Certificate from the object.
         */
        public X509Certificate getCertificate() {
            return certificate;
        }

        /**
         * @return The certificate fingerprint
         */
        public String getFingerprint() {
            return fingerprint;
        }
    }
}
