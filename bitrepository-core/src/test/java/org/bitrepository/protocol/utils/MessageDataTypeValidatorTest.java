package org.bitrepository.protocol.utils;

import org.apache.commons.codec.DecoderException;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MessageDataTypeValidatorTest {

    @Test
    void validateChecksumSpecTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            ChecksumSpecTYPE noChecksumTypeSpec = new ChecksumSpecTYPE();
            MessageDataTypeValidator.validate(noChecksumTypeSpec, "noChecksumTypeSpec");
        });
    }

    @Test
    void validateChecksumDataForFileNoChecksumTest() {
        assertThrows(IllegalArgumentException.class, () -> {
                    ChecksumDataForFileTYPE noChecksumSpec = new ChecksumDataForFileTYPE();
                    ChecksumSpecTYPE checksumTypeSpec = new ChecksumSpecTYPE();
                    checksumTypeSpec.setChecksumType(ChecksumType.MD5);
                    noChecksumSpec.setChecksumSpec(checksumTypeSpec);
                    noChecksumSpec.setCalculationTimestamp(CalendarUtils.getNow());
                    MessageDataTypeValidator.validate(noChecksumSpec, "noChecksumSpec");
                });
    }

    @Test
    void validateChecksumDataForFileNoTimestampTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            ChecksumDataForFileTYPE noChecksumSpec = new ChecksumDataForFileTYPE();
            ChecksumSpecTYPE checksumTypeSpec = new ChecksumSpecTYPE();
            checksumTypeSpec.setChecksumType(ChecksumType.MD5);
            noChecksumSpec.setChecksumSpec(checksumTypeSpec);
            noChecksumSpec.setChecksumValue(Base16Utils.encodeBase16("abab"));

            MessageDataTypeValidator.validate(noChecksumSpec, "noChecksumSpec");
        });
    }

    @Test
    void validateChecksumDataForFileNoChecksumSpecTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            ChecksumDataForFileTYPE noChecksumSpec = new ChecksumDataForFileTYPE();
            noChecksumSpec.setChecksumValue(Base16Utils.encodeBase16("abab"));
            noChecksumSpec.setCalculationTimestamp(CalendarUtils.getNow());

            MessageDataTypeValidator.validate(noChecksumSpec, "noChecksumSpec");
        });
    }

    //@Test(expectedExceptions = {IllegalArgumentException.class})
    public void validateChecksumDataForFileInvalidChecksumSpecTest() throws DecoderException {
        ChecksumDataForFileTYPE noChecksumSpec = new ChecksumDataForFileTYPE();
        ChecksumSpecTYPE checksumTypeSpec = new ChecksumSpecTYPE();
        noChecksumSpec.setChecksumSpec(checksumTypeSpec);
        noChecksumSpec.setChecksumValue(Base16Utils.encodeBase16("abab"));
        noChecksumSpec.setCalculationTimestamp(CalendarUtils.getNow());

        MessageDataTypeValidator.validate(noChecksumSpec, "noChecksumSpec");
    }

}
