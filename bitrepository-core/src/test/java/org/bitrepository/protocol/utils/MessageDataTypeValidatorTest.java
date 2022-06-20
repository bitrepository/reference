package org.bitrepository.protocol.utils;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.testng.annotations.Test;

public class MessageDataTypeValidatorTest {

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void validateChecksumSpecTest() {
        ChecksumSpecTYPE noChecksumTypeSpec = new ChecksumSpecTYPE();
        MessageDataTypeValidator.validate(noChecksumTypeSpec, "noChecksumTypeSpec");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void validateChecksumDataForFileNoChecksumTest() {
        ChecksumDataForFileTYPE noChecksumSpec = new ChecksumDataForFileTYPE();
        ChecksumSpecTYPE checksumTypeSpec = new ChecksumSpecTYPE();
        checksumTypeSpec.setChecksumType(ChecksumType.MD5);
        noChecksumSpec.setChecksumSpec(checksumTypeSpec);
        noChecksumSpec.setCalculationTimestamp(CalendarUtils.getNow());

        MessageDataTypeValidator.validate(noChecksumSpec, "noChecksumSpec");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void validateChecksumDataForFileNoTimestampTest() {
        ChecksumDataForFileTYPE noChecksumSpec = new ChecksumDataForFileTYPE();
        ChecksumSpecTYPE checksumTypeSpec = new ChecksumSpecTYPE();
        checksumTypeSpec.setChecksumType(ChecksumType.MD5);
        noChecksumSpec.setChecksumSpec(checksumTypeSpec);
        noChecksumSpec.setChecksumValue(Base16Utils.encodeBase16("abab"));

        MessageDataTypeValidator.validate(noChecksumSpec, "noChecksumSpec");
    }

    @Test(expectedExceptions = {IllegalArgumentException.class})
    public void validateChecksumDataForFileNoChecksumSpecTest() {
        ChecksumDataForFileTYPE noChecksumSpec = new ChecksumDataForFileTYPE();
        noChecksumSpec.setChecksumValue(Base16Utils.encodeBase16("abab"));
        noChecksumSpec.setCalculationTimestamp(CalendarUtils.getNow());

        MessageDataTypeValidator.validate(noChecksumSpec, "noChecksumSpec");
    }

    //@Test(expectedExceptions = {IllegalArgumentException.class})
    public void validateChecksumDataForFileInvalidChecksumSpecTest() {
        ChecksumDataForFileTYPE noChecksumSpec = new ChecksumDataForFileTYPE();
        ChecksumSpecTYPE checksumTypeSpec = new ChecksumSpecTYPE();
        noChecksumSpec.setChecksumSpec(checksumTypeSpec);
        noChecksumSpec.setChecksumValue(Base16Utils.encodeBase16("abab"));
        noChecksumSpec.setCalculationTimestamp(CalendarUtils.getNow());

        MessageDataTypeValidator.validate(noChecksumSpec, "noChecksumSpec");
    }

}
