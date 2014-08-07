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
 * Class to hold constants for used with the security module tests.
 */
public class SecurityTestConstants {

    
    /*
     * Notes about how to update the certificates, signature etc. 
     * TBD..   
     */
    private static final String DATA = "Hello world!";
    private static final String SIGNATURE = "MIAGCSqGSIb3DQEHAqCAMIACAQExDzANBglghkgBZQMEAgMFADCABgkqhkiG9w0BBwEAADGB1zCB1AIBATAuMCExCzAJBgNVBAYTAkRLMRIwEAYDVQQDDAljbGllbnQtMTMCCQDMZo0ssJ6s7zANBglghkgBZQMEAgMFADANBgkqhkiG9w0BAQEFAASBgHhp9p/wAHX8zAEIamAnyIywpI0wBYvR62pkLIrHwpTgsnjFpJRZPYYiF1egsIcy7ZjQrkh4UtMRLZyGbzk/GeuExdSrj66gAG4j8NeS7Ekp1zb16SUH8bKu/H83PqLxYBvIyEks3lMKu5T76Bmwa9x32H2zpzJjSqLRZCNgwQnBAAAAAAAA";
   
    /* probably expired certificate */
    private static final String POSITIVECERT = "-----BEGIN CERTIFICATE-----\n" +
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
    
    /* probably expired certificate */
    private static final String NEGATIVECERT =
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
    
    /* currently client100-certkey.pem */
    private static final String SIGNINGCERT =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIDkzCCAnugAwIBAgIJALlIlDh730tYMA0GCSqGSIb3DQEBBQUAMGAxCzAJBgNV\n" + 
            "BAYTAkRLMRAwDgYDVQQIDAdEZW5tYXJrMQ8wDQYDVQQHDAZBYXJodXMxGjAYBgNV\n" +
            "BAoMEUJpdHJlcG9zaXRvcnkub3JnMRIwEAYDVQQDDAljbGllbnQxMDAwHhcNMTQw\n" +
            "NzI5MTQwMzEyWhcNMjQwNzI2MTQwMzEyWjBgMQswCQYDVQQGEwJESzEQMA4GA1UE\n" +
            "CAwHRGVubWFyazEPMA0GA1UEBwwGQWFyaHVzMRowGAYDVQQKDBFCaXRyZXBvc2l0\n" +
            "b3J5Lm9yZzESMBAGA1UEAwwJY2xpZW50MTAwMIIBIjANBgkqhkiG9w0BAQEFAAOC\n" +
            "AQ8AMIIBCgKCAQEAr+8QNcRWPhyCmDt23K4WIRPLiwcu5jJHnopBWhQMp63K2ySX\n" +
            "j4iHXc4Qd9Ug+vGh2Max39I1xPfKJ5WliddAzzwh69R3ICQ2fyESRlaDN5RP9ngC\n" +
            "927CHbC2qgruVzM5AcsVWdv6NJi75peui0YkD2mYs8zKpgM4Ys5DeI6mfH9OAyvX\n" +
            "nn0QOZW3gTazBQccxWgBAGbMpyKsfsEh4nP8BDJEO82znK61K4qJ2c1+tlTwg2Nt\n" +
            "+aWz4mBiLnzZtZ8gJlspDMA0WpVgPlc6MU0kd+cJVCa4gbuWNoOm/ifoYS15RlWt\n" +
            "vQhnqFK7d9UwBv/fVM8NfdbJaeooyWdobf24IQIDAQABo1AwTjAdBgNVHQ4EFgQU\n" +
            "vFhO3LofBvKUKayjeLO4JCLxYewwHwYDVR0jBBgwFoAUvFhO3LofBvKUKayjeLO4\n" +
            "JCLxYewwDAYDVR0TBAUwAwEB/zANBgkqhkiG9w0BAQUFAAOCAQEABUjBPYK+6zWq\n" +
            "IOx/Thhr8ccTHitBLIYlN8mOy5v6odYtJpVo4/EuBNSSrY9w6EZA7fpp8u8X3xhg\n" +
            "Sp0Znd25SY6h1wWZztlP2RuPj1SI5pP10wEHD8sEUhimMnGbtxsEi6IaWRL9qki4\n" +
            "uLwKg6OyL9lxduaElS1hUFYMU5LXs9HdRzz8JHmlb6SfWLTvi6TRUZjvEUQ11TGJ\n" +
            "93qyZ0+1xJ9bKW3xB8yDybd67PkF7UYIyZXhGiMS9vlIeX7h107/4IT+EEe7vEyb\n" +
            "XfmVbCmlkuHZwJPLwawmOUACfYUDsk0A7dB5cSaMwcxYi4TNZOYII7rrSZ85UCgP\n" +
            "84Eon1U3cw==\n" +
            "-----END CERTIFICATE-----\n";
    
    

    private static final String KEYFILE = "./target/test-classes/client100-certkey.pem";
    
    private static final String ALLOWEDCERTIFICATEUSER = "test-component";

    private static final String COMPONENTID = "TEST";

    public static String getKeyFile() {
        return KEYFILE;
    }
    
    public static String getTestData() {
        return DATA;
    }
    
    public static String getSignature() {
        return SIGNATURE;
    }
    
    public static String getPositiveCertificate() {
        return POSITIVECERT;
    }
    
    public static String getNegativeCertificate() {
        return NEGATIVECERT;
    }
    
    public static String getSigningCertificate() {
        return SIGNINGCERT;
    }
    
    public static String getAllowedCertificateUser() {
        return ALLOWEDCERTIFICATEUSER;
    }
    
    public static String getDisallowedCertificateUser() {
        return ALLOWEDCERTIFICATEUSER + "-bad";
    }
    
    public static String getComponentID() {
        return COMPONENTID;
    }
    
    public static PermissionSet getDefaultPermissions() {
        PermissionSet permissions = new PermissionSet();
        ComponentIDs allowedUsers = new ComponentIDs();
        allowedUsers.getIDs().add(ALLOWEDCERTIFICATEUSER);
        
        Permission perm1 = new Permission();
        Certificate cert1 = new Certificate();
        cert1.setCertificateData(POSITIVECERT.getBytes());
        cert1.setAllowedCertificateUsers(allowedUsers);
        perm1.setCertificate(cert1);
        OperationPermission opPerm = new OperationPermission();
        opPerm.setOperation(Operation.GET_FILE);
        perm1.getOperationPermission().add(opPerm);
        
        Permission perm2 = new Permission();
        Certificate cert2 = new Certificate();
        cert2.setCertificateData(NEGATIVECERT.getBytes());
        cert2.setAllowedCertificateUsers(allowedUsers);
        perm2.setCertificate(cert2);
        
        permissions.getPermission().add(perm1);
        permissions.getPermission().add(perm2);

        return permissions;
    }
    
}
