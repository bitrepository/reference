/*
 * #%L
 * Bitrepository Webclient
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
