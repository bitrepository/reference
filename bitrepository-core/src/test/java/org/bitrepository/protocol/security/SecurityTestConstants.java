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
     * Notes about how to update the certificates, signature etc. found 
     * as comments in the variables below   
     */
    private static final String DATA = "Hello world!";
    // Use output from SignatureGeneratorTest to make a new signature if certificate changes
    private static final String SIGNATURE = "MIAGCSqGSIb3DQEHAqCAMIACAQExDzANBglghkgBZQMEAgMFADCABgkqhkiG9w0BBwEAADGCAZcwggGTAgEBMGwwXzELMAkGA1UEBhMCREsxEDAOBgNVBAgMB0Rlbm1hcmsxDzANBgNVBAcMBkFhcmh1czEaMBgGA1UECgwRQml0cmVwb3NpdG9yeS5vcmcxETAPBgNVBAMMCGNsaWVudDgwAgkAnwg/Emx5gB8wDQYJYIZIAWUDBAIDBQAwDQYJKoZIhvcNAQEBBQAEggEABkj3VQR6B5Lc0LZ7YAHvFCvDn2tCObPlLNpq0U1VRkLgIVMFQkZL4PpvG/MS/nEPI+VFz8Cty/UtODd23il6PnLjwwqYHKGCDnbJSg8YkX+UOO0NBr6Th6a6NWyeOElWiKxJ6pysXTV/b1DtY/uFFprLOPAgLVnp4TjZBtNuPFU+EBvvO1aFzKESpNPCs/qzF8Usf1ZqE+OPh49XLMqlKv4h8w1pjWLaZg4yCcqvPbqu1VjkvjNhkGHpc/k1whblBmMZ8JpdLlKCsWvj0+IuoztnClFQ/aDL1dkexnEaTf/CDhI8tFP1CEJZBUp0MjOtyPQbHGMe+1lxRWbsGYzusgAAAAAAAA==";
    // When certificate POSITIVECERT is changed, use openssl x509 -in <certificate.pem> -fingerprint to obtain new fingerprint
    private static final String FINGERPRINT = "D3:CC:F2:AE:36:4C:FB:85:F0:70:9A:59:8F:14:EF:8B:52:D4:A5:30";
    
    private static final String POSITIVECERT_KEYFILE = "./target/test-classes/client80-certkey.pem";
    
    /* currently client80-certkey.pem */
    private static final String POSITIVECERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDkTCCAnmgAwIBAgIJAJ8IPxJseYAfMA0GCSqGSIb3DQEBBQUAMF8xCzAJBgNV\n" +
            "BAYTAkRLMRAwDgYDVQQIDAdEZW5tYXJrMQ8wDQYDVQQHDAZBYXJodXMxGjAYBgNV\n" +
            "BAoMEUJpdHJlcG9zaXRvcnkub3JnMREwDwYDVQQDDAhjbGllbnQ4MDAeFw0xNDA3\n" +
            "MjkxNDAzMDlaFw0yNDA3MjYxNDAzMDlaMF8xCzAJBgNVBAYTAkRLMRAwDgYDVQQI\n" +
            "DAdEZW5tYXJrMQ8wDQYDVQQHDAZBYXJodXMxGjAYBgNVBAoMEUJpdHJlcG9zaXRv\n" +
            "cnkub3JnMREwDwYDVQQDDAhjbGllbnQ4MDCCASIwDQYJKoZIhvcNAQEBBQADggEP\n" +
            "ADCCAQoCggEBANjHnrczcJaZx5aczK3BpW71Rh2X44qUy2rw1sf65P3WP/vjCNQ0\n" +
            "7TgyaPxkTOBs34Uavkj6D0ocMNNcwVDS/BtqPR73iYBBBJmpR64AzGIh8jItgUuK\n" +
            "9X74eL2QrWnty78I1c4lsfq85Ua+MHfEfQcIbVaRrBplAAAbPqdan/2LwmVRNcmQ\n" +
            "3lBTT0UEUyshhygDvR/wljcDSNPAjTukor88O1mPh3hgBv4yPgmiwQe4BAh0/Imn\n" +
            "gtyLdmOFS2cQp2S8sFZS3LJ6kl7yaP74esnNmVmngIVtZ3jlP3xDgl7tV+4OSPjD\n" +
            "PNNhKIXl/ywEY1Q/hEkuNPbBI9nQTYplrIMCAwEAAaNQME4wHQYDVR0OBBYEFB1l\n" +
            "YxF7a4Blqm/WZ6jcTAb0nrF3MB8GA1UdIwQYMBaAFB1lYxF7a4Blqm/WZ6jcTAb0\n" +
            "nrF3MAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEFBQADggEBAATWp9ChCvLp22Xj\n" +
            "c84P+mq4eJhsOtHb/yxHjARpzVu4wi3Uvm9maQuYm7oNk3uhrzTLFDl5wHUUCZbL\n" +
            "0s3075it+jtBy3d5AGCDLbvoi8uiL2ngy/ScqttSx4ipXNTteM7UI7gnk3LwBVVb\n" +
            "4sFrtnAJNo8hT9rTJtD/6ZLDs0eYzoUshZW8Xbxd3h/1W+dfQiR7PO5Zqvqkmn+T\n" +
            "gko3OaiQMCz+IqE+/2tSMFlK7/DlpB/4MFECs5C7U9yqn9ulHEqo8vJF1rUjG5fL\n" +
            "YEn++kulWJnt4beI6UruCwCqCtBRKR38cPahK6Ic168h99ztO6JuvSm3v9LpDtXl\n" +
            "vSBi6TA=\n" +
            "-----END CERTIFICATE-----\n";
    
    /* currently client90-certkey.pem */
    private static final String NEGATIVECERT = "-----BEGIN CERTIFICATE-----\n" +
            "MIIDkTCCAnmgAwIBAgIJAK/RxZXju3LcMA0GCSqGSIb3DQEBBQUAMF8xCzAJBgNV\n" +
            "BAYTAkRLMRAwDgYDVQQIDAdEZW5tYXJrMQ8wDQYDVQQHDAZBYXJodXMxGjAYBgNV\n" +
            "BAoMEUJpdHJlcG9zaXRvcnkub3JnMREwDwYDVQQDDAhjbGllbnQ5MDAeFw0xNDA3\n" +
            "MjkxNDAzMTBaFw0yNDA3MjYxNDAzMTBaMF8xCzAJBgNVBAYTAkRLMRAwDgYDVQQI\n" +
            "DAdEZW5tYXJrMQ8wDQYDVQQHDAZBYXJodXMxGjAYBgNVBAoMEUJpdHJlcG9zaXRv\n" +
            "cnkub3JnMREwDwYDVQQDDAhjbGllbnQ5MDCCASIwDQYJKoZIhvcNAQEBBQADggEP\n" +
            "ADCCAQoCggEBAPkJis5DwU/1hha5Z6WZqqnBZqlcQWW3lSOn299UG4IqlMVmjidk\n" +
            "bc0+m1TGlk9ljnaDuwWWW70ushgOSGWXnskVkIYuUjHqrvf5AYGVH71kgYh9lf6F\n" +
            "GSayt2MCGrb2CTFJbmrBkKEPDNGynIjXd/J31gya9uOm7xf0K8ILe+HUn4U/4ukg\n" +
            "rDGQ9pKEfZNZtrKdtPRxvOiCPEuhr/wKCo9lcGsfCzHkpyA7vwmAL8z3h9F6ykNL\n" +
            "60MrI6bI4wyyDD3/rMTXJQq2IcgqnYL4NuG1dKLvO5XWdS6HvAxytcw+P6wnLKRJ\n" +
            "FO4EUN03Yy0j1yFUtkSp5jwD7yYAL3MEdDECAwEAAaNQME4wHQYDVR0OBBYEFLj+\n" +
            "l1tHSxDKqiHz51ICD/snnN35MB8GA1UdIwQYMBaAFLj+l1tHSxDKqiHz51ICD/sn\n" +
            "nN35MAwGA1UdEwQFMAMBAf8wDQYJKoZIhvcNAQEFBQADggEBADiEZhWN9BfxbU0N\n" +
            "HHmxvW64nNL7mZM1EYak6xGnOg9eryvi02AQMsFosmID4LC1330T9CAzmH5sS8Dx\n" +
            "62r10ZCaCFMZG1JD2IN24mlantizMzcFDqyl+zaAPfSU3RgjNsD2vjxgB4f9vLY4\n" +
            "kcQqIV7MAg6pjQOhGEuiQU67+X45DnpVO6pfS9FTafuAg2ogYlTmb1ONQfQw2msc\n" +
            "3jEDtD878eOHXHhSKU7doN3ymikSUopaAXG/y8zPcH+eYqKbHGYov+nYDfz9MoLp\n" +
            "ldNjVbpoXMzQcYxC1kzgjAXgazxis4q1DQaAnCfIp3VstK+ilbeZA3p7Tda3Zh/H\n" +
            "njr8Biw=\n" +
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

    public static String getPositiveCertKeyFile() {
        return POSITIVECERT_KEYFILE;
    }
    
    public static String getKeyFile() {
        return KEYFILE;
    }
    
    public static String getTestData() {
        return DATA;
    }
    
    public static String getFingerprintForSignatureCert() {
        return FINGERPRINT;
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
