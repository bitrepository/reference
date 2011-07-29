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

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.ConfigurationException;

/**
 * Utility class for handling checksum calculations.
 */
public final class ChecksumUtils {

    /** The magical integer '4'.*/
    private static final int MAGIC_INTEGER_4 = 4;
    /** The magical integer for the hexadecimal '0x0F'.*/
    private static final int MAGIC_INTEGER_OXOF = 0x0F;
    /** The maximal size of the byte array for digest.*/
    private static final int BYTE_ARRAY_SIZE_FOR_DIGEST = 4000;

    /** 
     * Private constructor. To prevent instantiation of this utility class.
     */
    private ChecksumUtils() { }

    /**
     * Calculates the checksum for a file based on the given checksum algorithm, though calculated with a given salt.
     * 
     * @param file The file to calculate the checksum for.
     * @param messageDigest The digest algorithm to use for calculating the checksum of the file.
     * @param salt The string to add in front of the data stream before calculating the checksum. Null or the empty 
     * string means no salt. 
     * @return The checksum of the file in hexadecimal.
     */
    public static String generateChecksum(File file, MessageDigest messageDigest, String salt) {
        ArgumentValidator.checkNotNull(messageDigest, "MessageDigest messageDigest");
        ArgumentValidator.checkNotNull(file, "File file");
        
        byte[] bytes = new byte[BYTE_ARRAY_SIZE_FOR_DIGEST];
        int bytesRead;
        try {
            DataInputStream fis = null;
            try {
                fis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                messageDigest.reset();
                // first add the salt, if any
                if(salt != null && !salt.isEmpty()) {
                    messageDigest.update(salt.getBytes(), 0, salt.length());
                }
                // Then add the actual message.
                while ((bytesRead = fis.read(bytes)) > 0) {
                    messageDigest.update(bytes, 0, bytesRead);
                }
            } finally {
                if (fis != null) {
                    fis.close();
                }
            }
        } catch (IOException e) {
            throw new ConfigurationException("Could not calculate the checksum with algorithm '" + messageDigest 
                    + "' for file '" + file.getAbsolutePath() + "'.", e);
        }
        return toHex(messageDigest.digest());
    }
    
    /**
     * Calculates the checksum for a file based on the given checksum algorithm. This is calculated without any salt.
     * 
     * @param file The file to calculate the checksum for.
     * @param messageDigest The name of the digest algorithm to use for calculating the checksum of the file.
     * @return The checksum of the file in hexadecimal.
     */
    public static String generateChecksum(File file, MessageDigest messageDigest) {
        return generateChecksum(file, messageDigest, null);
    }
    
    /**
     * Calculates the checksum for a file based on the given checksum algorith, though calculated with a given salt.
     * 
     * @param file The file to calculate the checksum for.
     * @param messageDigest The digest algorithm to use for calculating the checksum of the file.
     * @param salt The string to add in front of the data stream before calculating the checksum. Null or the empty 
     * string means no salt.
     * @return The checksum of the file in hexadecimal.
     */
    public static String generateChecksum(File file, String messageDigestName, String salt) {
        try {
            return generateChecksum(file, MessageDigest.getInstance(messageDigestName), salt);
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
}
