package org.bitrepository.webservice;

import java.net.MalformedURLException;
import java.net.URL;

public class WebserviceInputChecker {

    public static void checkFileIDParameter(String fileID) throws WebserviceIllegalArgumentException {
        if(fileID == null || fileID.isEmpty()) {
            throw new WebserviceIllegalArgumentException("Failure: missing fileID parameter."); 
        }
    }
    
    public static void checkFileSizeParameter(String fileSize) throws WebserviceIllegalArgumentException {
        if(fileSize == null || fileSize.isEmpty()) {
            throw new WebserviceIllegalArgumentException("Failure: missing filesize");
        }
        try {
            Long.parseLong(fileSize);
        } catch (Exception e) {
            throw new WebserviceIllegalArgumentException("Failure: " + fileSize + " is not a valid number value");
        }
    }
    
    public static void checkURLParameter(String URL) throws WebserviceIllegalArgumentException {
        if(URL == null || URL.isEmpty()) {
            throw new  WebserviceIllegalArgumentException("Failure: missing url parameter.");
        }
        @SuppressWarnings("unused")
        URL url;
        try {
            url = new URL(URL);
        } catch (MalformedURLException e) {
            throw new  WebserviceIllegalArgumentException("Failure: malformed url parameter.");
        }
    }
    
    public static void checkChecksumTypeParameter(String checksumType) throws WebserviceIllegalArgumentException {
        if(checksumType == null || checksumType.isEmpty()) {
            throw new WebserviceIllegalArgumentException("Failure: missing checksumType parameter."); 
        }
        // check for supported types?
    }
    
    public static void checkPillarIDParameter(String pillarID) throws WebserviceIllegalArgumentException {
        if(pillarID == null || pillarID.isEmpty()) {
            throw new WebserviceIllegalArgumentException("Failure: missing pillarID parameter."); 
        }
    }
   
    public static void checkChecksumParameter(String checksum) throws WebserviceIllegalArgumentException {
        if(checksum != null && !checksum.isEmpty()) {
            if((checksum.length() % 2) != 0) {
                throw new WebserviceIllegalArgumentException("Failure: checksum parameter contains an uneven number of characters.");
            }
            if(!checksum.matches("^\\p{XDigit}*$")) {
                throw new WebserviceIllegalArgumentException("Failure: checksum parameter contains a non hexadecimal value.");
            }
        }    
    }
    
    public static void checkSaltParameter(String salt) throws WebserviceIllegalArgumentException {
        if(salt != null && !salt.isEmpty()) {
            if((salt.length() % 2) != 0) {
                throw new WebserviceIllegalArgumentException("Failure: salt parameter contains an uneven number of characters.");
            }
            if(!salt.matches("^\\p{XDigit}*$")) {
                throw new WebserviceIllegalArgumentException("Failure: salt parameter contains a non hexadecimal value.");
            }
        }    
    }
}
