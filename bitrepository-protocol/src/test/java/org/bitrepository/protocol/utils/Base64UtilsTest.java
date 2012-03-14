package org.bitrepository.protocol.utils;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Utility class for handling encoding and decoding of base64 bytes.
 */
public class Base64UtilsTest extends ExtendedTestCase {
    
    private final String DECODED_CHECKSUM = "ff5aca7ae8c80c9a3aeaf9173e4dfd27";
    private final byte[] ENCODED_CHECKSUM = new byte[]{-1,90,-54,122,-24,-56,12,-102,58,-22,-7,23,62,77,-3,39};

    @Test(groups = { "regressiontest" })
    public void encodeChecksum() throws Exception {
        addDescription("Validating the encoding of the checksums.");
        addStep("Encode the checksum and validate", "It should match the precalculated constant.");
        byte[] encodedChecksum = Base64Utils.encodeBase64(DECODED_CHECKSUM);
        
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
        String decodedChecksum = Base64Utils.decodeBase64(ENCODED_CHECKSUM);
        Assert.assertEquals(decodedChecksum, DECODED_CHECKSUM);
    }
}
