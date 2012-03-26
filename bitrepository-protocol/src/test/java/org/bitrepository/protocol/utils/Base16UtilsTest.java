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

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Utility class for handling encoding and decoding of base64 bytes.
 */
public class Base16UtilsTest extends ExtendedTestCase {
    
    private final String DECODED_CHECKSUM = "ff5aca7ae8c80c9a3aeaf9173e4dfd27";
    private final byte[] ENCODED_CHECKSUM = new byte[]{-1,90,-54,122,-24,-56,12,-102,58,-22,-7,23,62,77,-3,39};

    @Test(groups = { "regressiontest" })
    public void encodeChecksum() throws Exception {
        addDescription("Validating the encoding of the checksums.");
        addStep("Encode the checksum and validate", "It should match the precalculated constant.");
        byte[] encodedChecksum = Base16Utils.encodeBase16(DECODED_CHECKSUM);
        
        Assert.assertEquals(encodedChecksum.length, ENCODED_CHECKSUM.length, 
                "The size of the encoded checksum differs from the expected.");
        
        for(int i = 0; i < encodedChecksum.length; i++){
            Assert.assertEquals(encodedChecksum[i], ENCODED_CHECKSUM[i]);
        }
    }
    
    @Test(groups = { "regressiontest" })
    public void decodeChecksum() throws Exception {
        addDescription("Validating the decoding of the checksums.");
        addStep("Decode the checksum and validate.", "It should match the precalculated constant.");
        String decodedChecksum = Base16Utils.decodeBase16(ENCODED_CHECKSUM);
        Assert.assertEquals(decodedChecksum, DECODED_CHECKSUM);
    }
}
