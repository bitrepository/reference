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
package org.bitrepository.common.utils;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bitrepository.common.ArgumentValidator;

/**
 * Utility class for handling encoding and decoding of base16 bytes.
 */
public class Base16Utils {
    private Base16Utils() {
    }

    /**
     * Decodes a base16 encoded byte set into a human-readable string.
     *
     * @param data The data to decode.
     * @return The decoded data, or null if a null is given.
     */
    public static String decodeBase16(byte[] data) {
        if (data == null) {
            return null;
        }
        // TODO Java 17 has HexFormat.of().formatHex(bytes) - consider using instead
        return Hex.encodeHexString(data);
    }

    /**
     * Encodes a hex string to a set of base16 bytes.
     *
     * @param hexString The string to encode to base16.
     * @return The string encoded to base16.
     */
    public static byte[] encodeBase16(String hexString) {
        ArgumentValidator.checkNotNullOrEmpty(hexString, "String hexString");
        // TODO Java 17 has HexFormat.of().parseHex(s) - consider using instead
        try {
            return Hex.decodeHex(hexString);
        } catch (DecoderException e) {
            throw new IllegalArgumentException("Bad hex-string '" + hexString + "': " + e.getMessage());
        }
    }
}
