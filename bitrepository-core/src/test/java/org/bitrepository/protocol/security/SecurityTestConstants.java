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

import org.bitrepository.settings.collectionsettings.Certificate;
import org.bitrepository.settings.collectionsettings.ComponentIDs;
import org.bitrepository.settings.collectionsettings.Operation;
import org.bitrepository.settings.collectionsettings.OperationPermission;
import org.bitrepository.settings.collectionsettings.Permission;
import org.bitrepository.settings.collectionsettings.PermissionSet;

/**
 * Class to hold constants for used with the security module tests.
 */
public class SecurityTestConstants {

    private static final String data = "Hello world!";
    private static final String signature = "MIAGCSqGSIb3DQEHAqCAMIACAQExDzANBglghkgBZQMEAgMFADCABgkqhkiG9w0BBwEAADGB1zCB1AIBATAuMCExCzAJBgNVBAYTAkRLMRIwEAYDVQQDDAljbGllbnQtMTMCCQDMZo0ssJ6s7zANBglghkgBZQMEAgMFADANBgkqhkiG9w0BAQEFAASBgHhp9p/wAHX8zAEIamAnyIywpI0wBYvR62pkLIrHwpTgsnjFpJRZPYYiF1egsIcy7ZjQrkh4UtMRLZyGbzk/GeuExdSrj66gAG4j8NeS7Ekp1zb16SUH8bKu/H83PqLxYBvIyEks3lMKu5T76Bmwa9x32H2zpzJjSqLRZCNgwQnBAAAAAAAA";
   
    private static final String positiveCert = "-----BEGIN CERTIFICATE-----\n" +
            "MIIBuTCCASICCQDMZo0ssJ6s7zANBgkqhkiG9w0BAQUFADAhMQswCQYDVQQG\n" +
            "EwJESzESMBAGA1UEAwwJY2xpZW50LTEzMB4XDTExMTAyMTA5MjAwMVoXDTE0\n" +
            "MDcxNzA5MjAwMVowITELMAkGA1UEBhMCREsxEjAQBgNVBAMMCWNsaWVudC0x\n" +
            "MzCBnzANBgkqhkiG9w0BAQEFAAOBjQAwgYkCgYEA6DE31oL3v3tuZilsJ4YK\n" +
            "0fnBRuShVahIh6yTv7BIY6t1+DAT/N+fcnTU73IKGLH+2X67oa3/YhcoySju\n" +
            "Ei0ZehqvTruKH7UAetS2aPsJBiuWX3giJQkhN62E8a5b63A9Aw3iokuoVWd5\n" +
            "Ohm+0Ra+6tcZ/IxWsWRcM8RWjOJb6vcCAwEAATANBgkqhkiG9w0BAQUFAAOB\n" +
            "gQBu3OgpXt/0WluSBmjDPiavLor3lqDoJBGTMn0mr05g0gZFhSfI4vIj5kvW\n" +
            "QUWR/yBgW0chzA+GZHwctaLQyTxp0AT/F4VsTtlN3YpBbeMlOK/BC+w9MpAO\n" +
            "me0coE/bZzOuq3gQ15XOkelIxmnrh2xnGotE6thmFFClT6VY8mqEFA==\n" +
            "-----END CERTIFICATE-----\n";
    
    private static final String negativeCert =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIICCzCCAXQCCQCHLeckUtZcJDANBgkqhkiG9w0BAQUFADBKMQswCQYDVQQGEwJESzEgMB4GA1UE\n" +
            "ChMXRGV0IEtvbmdlbGlnZSBCaWJsaW90ZWsxDDAKBgNVBAsTA0RJUzELMAkGA1UEAxMCQ0EwHhcN\n" +
            "MTEwOTI4MTExNjQ1WhcNMTMwNDI5MjIyMDEzWjBKMQswCQYDVQQGEwJESzEgMB4GA1UEChMXRGV0\n" +
            "IEtvbmdlbGlnZSBCaWJsaW90ZWsxDDAKBgNVBAsTA0RJUzELMAkGA1UEAxMCQ0EwgZ8wDQYJKoZI\n" +
            "hvcNAQEBBQADgY0AMIGJAoGBAJcGvaV2VjjIhq0NGD1sCDPw/Xvu/G0zzJLStStbvAQZ95CKZ52V\n" +
            "CM7oQ4Ge4Qse+sNNL+DU9ENzFoN/1Xvqip1e0B204arErZaRXc4lThW3vTt7JWx9s/l2TOxnsCuq\n" +
            "uXhe+VnQkMdGu1WeSKIgzhxJ5vjV5mPXkj/RsVnKSp+PAgMBAAEwDQYJKoZIhvcNAQEFBQADgYEA\n" +
            "VbQ5VPPDOCW0wuyMLFu8W2W0Tvplv8A458w37qNVo3pvznDSVdEOpPIRznTIM836XSwHWCWhRPN/\n" +
            "Mo2U+CRkSEaN8nPkqxOY46w1AKqhhgLAPr6/sOCjG6k6jxEITYzYO5mv0nAg4yAVvfE4O715pjwO\n" +
            "77h9LapqyJ8S1GSKHr8=\n" +
            "-----END CERTIFICATE-----\n";
    
    private static final String signingCert = 
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIBuTCCASICCQDkYepx9PPiZTANBgkqhkiG9w0BAQUFADAhMQswCQYDVQQGEwJE\n" + 
            "SzESMBAGA1UEAwwJY2xpZW50LTE5MB4XDTExMTAyMTA5MjAwMloXDTE0MDcxNzA5\n" +
            "MjAwMlowITELMAkGA1UEBhMCREsxEjAQBgNVBAMMCWNsaWVudC0xOTCBnzANBgkq\n" +
            "hkiG9w0BAQEFAAOBjQAwgYkCgYEAwEKO1j00Pqvjnz1vy1uYqvFfog9v0IRu4izw\n" +
            "Iu08bJFg5t4fLWOyGHiVipf+gJNjGjnpEk1Hxw3by+g9WmGGkmbEg+7LNIpt6GYE\n" +
            "U88WCobyZPnych7+WHMFSXgdboNfc7nay3h/KA4ugUE0fGSfJNtGizQEal/R/ZPQ\n" +
            "aOGVu8cCAwEAATANBgkqhkiG9w0BAQUFAAOBgQCLQlPI6kQnwxk+BgwGaB7Lx880\n" +
            "DCSOT5baDyyL+VsdoXN7vPuwYlZkMEfP3VcSM47gS2O6UglmuTKtYeWwpwGThyKx\n" +
            "jjiq5zw/JpS9+0WT+rE9MR2havPycSOf8hYFBSqN3PSFMmIGWM1VaONa7mal9arh\n" +
            "2LUhJmUulxjtOw4ZLA==\n" +
            "-----END CERTIFICATE-----\n";

    private static final String KEYFILE = "./target/test-classes/client-19.pem";
    
    private static final String allowedCertificateUser = "test-component";

    private static final String componentID = "TEST";

    public static String getKeyFile() {
        return KEYFILE;
    }
    
    public static String getTestData() {
        return data;
    }
    
    public static String getSignature() {
        return signature;
    }
    
    public static String getPositiveCertificate() {
        return positiveCert;
    }
    
    public static String getNegativeCertificate() {
        return negativeCert;
    }
    
    public static String getSigningCertificate() {
        return signingCert;
    }
    
    public static String getAllowedCertificateUser() {
        return allowedCertificateUser;
    }
    
    public static String getDisallowedCertificateUser() {
        return allowedCertificateUser + "-bad";
    }
    
    public static String getComponentID() {
        return componentID;
    }
    
    public static PermissionSet getDefaultPermissions() {
        PermissionSet permissions = new PermissionSet();
        ComponentIDs allowedUsers = new ComponentIDs();
        allowedUsers.getIDs().add(allowedCertificateUser);
        
        Permission perm1 = new Permission();
        Certificate cert1 = new Certificate();
        cert1.setCertificateData(positiveCert.getBytes());
        cert1.setAllowedCertificateUsers(allowedUsers);
        perm1.setCertificate(cert1);
        OperationPermission opPerm = new OperationPermission();
        opPerm.setOperation(Operation.GET_FILE);
        perm1.getOperationPermission().add(opPerm);
        
        Permission perm2 = new Permission();
        Certificate cert2 = new Certificate();
        cert2.setCertificateData(negativeCert.getBytes());
        cert2.setAllowedCertificateUsers(allowedUsers);
        perm2.setCertificate(cert2);
        
        permissions.getPermission().add(perm1);
        permissions.getPermission().add(perm2);

        return permissions;
    }
    
}
