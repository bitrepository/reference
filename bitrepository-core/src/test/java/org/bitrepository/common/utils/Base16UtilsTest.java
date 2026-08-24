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
import org.bitrepository.TestGroups;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

/**
 * Utility class for handling encoding and decoding of base64 bytes.
 */
class Base16UtilsTest {

    private final String DECODED_CHECKSUM = "ff5aca7ae8c80c9a3aeaf9173e4dfd27";
    private final byte[] ENCODED_CHECKSUM =
            new byte[]{-1, 90, -54, 122, -24, -56, 12, -102, 58, -22, -7, 23, 62, 77, -3, 39};

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void encodeChecksum() throws Exception {
        addDescription("Validating the encoding of the checksums.");
        addStep("Encode the checksum and validate", "It should match the precalculated constant.");
        byte[] encodedChecksum = Base16Utils.encodeBase16(DECODED_CHECKSUM);

        Assertions.assertEquals(ENCODED_CHECKSUM.length, encodedChecksum.length,
                "The size of the encoded checksum differs from the expected.");

        for (int i = 0; i < encodedChecksum.length; i++) {
            Assertions.assertEquals(ENCODED_CHECKSUM[i], encodedChecksum[i]);
        }
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void decodeChecksum() {
        addDescription("Validating the decoding of the checksums.");
        addStep("Decode the checksum and validate.", "It should match the precalculated constant.");
        String decodedChecksum = Base16Utils.decodeBase16(ENCODED_CHECKSUM);
        Assertions.assertEquals(DECODED_CHECKSUM, decodedChecksum);
    }

    @Test
    void decodesNull() {
        addDescription("Test decoding null");
        byte[] data = null;
        String decoded = Base16Utils.decodeBase16(data);
        Assertions.assertNull(decoded);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void badArgumentTest() {
        addDescription("Test bad arguments");
        Assertions.assertThrows(IllegalArgumentException.class, () -> Base16Utils.encodeBase16(null));

        addStep("Test with an odd number of characters.", "Should throw a decoder exception");
        Assertions.assertThrows(DecoderException.class, () -> Base16Utils.encodeBase16("123"));

        addStep("Test with a non hex digit.", "Should throw a decoder exception");
        Assertions.assertThrows(DecoderException.class, () -> Base16Utils.encodeBase16("1g"));
    }
}
