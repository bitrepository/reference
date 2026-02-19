/*
 * #%L
 * Bitrepository Command Line
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
package org.bitrepository.commandline.utils;

import org.apache.commons.cli.Option;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.client.DefaultFixtureClientTest;
import org.bitrepository.commandline.Constants;
import org.bitrepository.commandline.output.OutputHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


public class ChecksumExtractionUtilsTest extends DefaultFixtureClientTest {
    CommandLineArgumentsHandler cmdHandler;
    OutputHandler output;

    @BeforeEach
    public void setup() {
        cmdHandler = new CommandLineArgumentsHandler();
        cmdHandler.addOption(new Option(Constants.REQUEST_CHECKSUM_SALT_ARG, Constants.HAS_ARGUMENT, ""));
        cmdHandler.addOption(new Option(Constants.REQUEST_CHECKSUM_TYPE_ARG, Constants.HAS_ARGUMENT, ""));
        output = Mockito.mock(OutputHandler.class);
    }

    @Test
    @Tag("regressiontest")
    public void testDefaultChecksumSpec() throws Exception {
        addDescription("Test that the default checksum is retrieved when no arguments are given.");
        cmdHandler.parseArguments();
        ChecksumType type = ChecksumExtractionUtils.extractChecksumType(cmdHandler, settingsForCUT, output);
        Assertions.assertEquals(settingsForCUT.getRepositorySettings().getProtocolSettings().getDefaultChecksumType(), type.name());
    }

    @Test
    @Tag("regressiontest")
    public void testDefaultChecksumSpecWithSaltArgument() throws Exception {
        addDescription("Test that the HMAC version of default checksum is retrieved when the salt arguments are given.");
        cmdHandler.parseArguments("-" + Constants.REQUEST_CHECKSUM_SALT_ARG + "0110");
        ChecksumType type = ChecksumExtractionUtils.extractChecksumType(cmdHandler, settingsForCUT, output);
        Assertions.assertEquals("HMAC_" + settingsForCUT.getRepositorySettings().getProtocolSettings().getDefaultChecksumType(), type.name());
    }

    @Test
    @Tag("regressiontest")
    public void testNonSaltChecksumSpecWithoutSaltArgument() throws Exception {
        addDescription("Test that a non-salt checksum type is retrieved when it is given as argument, " +
                "and no salt arguments are given.");
        ChecksumType enteredType = ChecksumType.SHA384;
        cmdHandler.parseArguments("-" + Constants.REQUEST_CHECKSUM_TYPE_ARG + enteredType);
        ChecksumType type = ChecksumExtractionUtils.extractChecksumType(cmdHandler, settingsForCUT, output);
        Assertions.assertEquals(enteredType, type);
    }

    @Test
    @Tag("regressiontest")
    public void testNonSaltChecksumSpecWithSaltArgument() throws Exception {
        addDescription("Test that a salt checksum type is retrieved even though a non-salt checksum algorithm it is given as argument, "
                + "but a salt argument also is given.");
        ChecksumType enteredType = ChecksumType.SHA512;
        cmdHandler.parseArguments("-" + Constants.REQUEST_CHECKSUM_TYPE_ARG + enteredType,
                "-" + Constants.REQUEST_CHECKSUM_SALT_ARG + "0110");
        ChecksumType type = ChecksumExtractionUtils.extractChecksumType(cmdHandler, settingsForCUT, output);
        Assertions.assertNotEquals(enteredType, type);
        Assertions.assertEquals("HMAC_" + enteredType.name(), type.name());
    }

    @Test
    @Tag("regressiontest")
    public void testSaltChecksumSpecWithoutSaltArgument() throws Exception {
        addDescription("Test that a non-salt checksum type is retrieved even though a salt checksum algorithm it is given as argument, "
                + "but no salt argument also is given.");
        ChecksumType enteredType = ChecksumType.HMAC_SHA256;
        cmdHandler.parseArguments("-" + Constants.REQUEST_CHECKSUM_TYPE_ARG + enteredType);
        ChecksumType type = ChecksumExtractionUtils.extractChecksumType(cmdHandler, settingsForCUT, output);
        Assertions.assertNotEquals(enteredType, type);
        Assertions.assertTrue(enteredType.name().contains("HMAC"));
        Assertions.assertEquals(enteredType.name().replace("HMAC_", ""), type.name());
    }

    @Test
    @Tag("regressiontest")
    public void testSaltChecksumSpecWithSaltArgument() throws Exception {
        addDescription("Test that a salt checksum type is retrieved when the salt checksum algorithm it is given as argument, "
                + "and a salt argument also is given.");
        ChecksumType enteredType = ChecksumType.HMAC_SHA256;
        cmdHandler.parseArguments("-" + Constants.REQUEST_CHECKSUM_TYPE_ARG + enteredType,
                "-" + Constants.REQUEST_CHECKSUM_SALT_ARG + "0110");
        ChecksumType type = ChecksumExtractionUtils.extractChecksumType(cmdHandler, settingsForCUT, output);
        Assertions.assertEquals(enteredType, type);
    }
}
