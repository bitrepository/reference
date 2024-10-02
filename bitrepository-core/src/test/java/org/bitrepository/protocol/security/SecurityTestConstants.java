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

import org.bitrepository.settings.repositorysettings.Certificate;
import org.bitrepository.settings.repositorysettings.ComponentIDs;
import org.bitrepository.settings.repositorysettings.Operation;
import org.bitrepository.settings.repositorysettings.OperationPermission;
import org.bitrepository.settings.repositorysettings.Permission;
import org.bitrepository.settings.repositorysettings.PermissionSet;

/**
 * Class to hold constants for use with the security module tests.
 */
public class SecurityTestConstants {
    private static final String DATA = "Hello world!";

    private static final String POSITIVE_CERT_KEYFILE = "./target/test-classes/client80-certkey.pem";

    private static final String KEYFILE = "./target/test-classes/client100-certkey.pem";

    private static final String ALLOWED_CERTIFICATE_USER = "test-component";

    private static final String COMPONENT_ID = "TEST";

    public static String getPositiveCertKeyFile() {
        return POSITIVE_CERT_KEYFILE;
    }

    public static String getKeyFile() {
        return KEYFILE;
    }

    public static String getTestData() {
        return DATA;
    }

    public static String getAllowedCertificateUser() {
        return ALLOWED_CERTIFICATE_USER;
    }

    public static String getDisallowedCertificateUser() {
        return ALLOWED_CERTIFICATE_USER + "-bad";
    }

    public static String getComponentID() {
        return COMPONENT_ID;
    }

    public static PermissionSet getDefaultPermissions() throws Exception {
        PermissionSet permissions = new PermissionSet();
        ComponentIDs allowedUsers = new ComponentIDs();
        allowedUsers.getIDs().add(ALLOWED_CERTIFICATE_USER);

        Permission perm1 = new Permission();
        Certificate cert1 = new Certificate();
        byte[] positiveCert = TestCertProvider.loadPositiveCert().getEncoded();
        cert1.setCertificateData(positiveCert);
        cert1.setAllowedCertificateUsers(allowedUsers);
        perm1.setCertificate(cert1);
        OperationPermission opPerm = new OperationPermission();
        opPerm.setOperation(Operation.GET_FILE);
        perm1.getOperationPermission().add(opPerm);

        Permission perm2 = new Permission();
        Certificate cert2 = new Certificate();
        byte[] negativeCert = TestCertProvider.loadNegativeCert().getEncoded();
        cert2.setCertificateData(negativeCert);
        cert2.setAllowedCertificateUsers(allowedUsers);
        perm2.setCertificate(cert2);

        permissions.getPermission().add(perm1);
        permissions.getPermission().add(perm2);

        return permissions;
    }
}
