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
package org.bitrepository.protocol.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for handling encoding and decoding of base64 bytes.
 */
public class Base64Utils {
    /** The log.*/
    private static final Logger log = LoggerFactory.getLogger(Base64Utils.class);
    
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private Base64Utils() {}
    
    /**
     * Decodes a Base64 encoded byte set into a human readable string.
     * @param data The data to decode.
     * @return The decoded data.
     */
    public static String decodeBase64(byte[] data) {
        StringBuffer sb = new StringBuffer(data.length * 2);
        for (int i = 0; i < data.length; i++){
          int v = data[i] & 0xff;
          if (v < 16) {
            sb.append('0');
          }
          sb.append(Integer.toHexString(v));
        }
        log.debug("Base64 decoded byte array to string, string: " + sb.toString());
        return sb.toString();
    }
    
    /**
     * Encoding a hex string to base64.
     * 
     * @param hexString The string to encode to base64.
     * @return The string encoded to base64.
     */
    public static byte[] encodeBase64(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                                 + Character.digit(hexString.charAt(i+1), 16));
        }
        
        log.debug("Base64 encoded string to byte array, string: " + hexString);
        return data;
    }
}
