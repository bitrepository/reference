/*
 * #%L
 * Bitrepository Modifying Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.common.utils;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility class for handling checksum calculations.
 * Uses the HMAC method for calculating the checksums with salt.
 */
public final class ChecksumUtils {

    /** The magical integer '4'.*/
    private static final int MAGIC_INTEGER_4 = 4;
    /** The magical integer for the hexadecimal '0x0F'.*/
    private static final int MAGIC_INTEGER_OXOF = 0x0F;

    /** 
     * Private constructor. To prevent instantiation of this utility class.
     */
    private ChecksumUtils() { }

    /**
     * Calculates the checksum for a file based on the given checksum algorith, where the calculcation is salted.
     * 
     * @param file The file to calculate the checksum for.
     * @param messageDigest The digest algorithm to use for calculating the checksum of the file.
     * @param salt The string to add in front of the data stream before calculating the checksum. Null or the empty 
     * string means no salt.
     * @return The checksum of the file in hexadecimal.
     */
    public static String generateChecksum(File file, String algorithm, String salt) {
        try {
            return generateChecksum(FileUtils.readFile(file), algorithm, salt.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Calculates the checksum for a file based on the given checksum algorith, where the calculcation is salted.
     * 
     * @param file The file to calculate the checksum for.
     * @param messageDigest The digest algorithm to use for calculating the checksum of the file.
     * @param salt The string to add in front of the data stream before calculating the checksum. Null or the empty 
     * string means no salt.
     * @return The checksum of the file in hexadecimal.
     */
    public static String generateChecksum(File file, String algorithm, byte[] salt) {
        try {
            return generateChecksum(FileUtils.readFile(file), algorithm, salt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Calculates a checksum based on 
     * 
     * @param content The string to calculate from.
     * @param algorithm The algorithm to use for calculation. If it is not prefixed with 'Hmac', then it is added.
     * @param salt The salt for the calculation. 
     * @return The HMAC calculated checksum in hexadecimal.
     */
    public static String generateChecksum(String content, String algorithm, byte[] salt) {
        try {
            String algorithmName = algorithm.toUpperCase();
            if(!algorithm.startsWith("Hmac")) {
                algorithmName = "Hmac" + algorithm.toUpperCase();
            }
            
            Mac messageAuthenticationCode = Mac.getInstance(algorithmName);
            Key key;
            
            if(salt == null || salt.length == 0) {
                key = new SecretKeySpec(new byte[]{0}, algorithmName);
            } else {
                key = new SecretKeySpec(salt, algorithmName);
            }
            
            messageAuthenticationCode.init(key);
            byte[] digest = messageAuthenticationCode.doFinal(content.getBytes());
            
            return toHex(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Method for extracting a array of bytes as hex.
     * 
     * @param byteArray The byte array to make into hex.
     * @return The hexadecimal representation of the byte array.
     */
    private static String toHex(byte[] byteArray) {
        // The list of value representations for hexadecimals.
        char[] hexdigit = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        StringBuffer sb = new StringBuffer("");
        for (byte b : byteArray) {
            sb.append(hexdigit[(b >> MAGIC_INTEGER_4) & MAGIC_INTEGER_OXOF]);
            sb.append(hexdigit[b & MAGIC_INTEGER_OXOF]);
        }
        return sb.toString();
    }
    
    /**
     * Returns whether a given checksum calculation algorithm exists.
     * This only checksum whether the algorithm exists for HMAC. If it is not prefixed with 'Hmac', then it is added. 
     * 
     * @param algorithm The algorithm for the checksum calculation to test.
     * @throws NoSuchAlgorithmException If the algorithm does not exist.
     */
    public static void verifyAlgorithm(String algorithm) throws NoSuchAlgorithmException {
        String algorithmName = algorithm.toUpperCase();
        if(!algorithm.startsWith("Hmac")) {
            algorithmName = "Hmac" + algorithm.toUpperCase();
        }
        
        Mac.getInstance(algorithmName);
    }
}
