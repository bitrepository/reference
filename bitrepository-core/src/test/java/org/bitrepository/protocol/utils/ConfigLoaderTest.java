/*
 * #%L
 * Bitrepository Core
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

import org.bitrepository.common.utils.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


import java.io.File;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

public class ConfigLoaderTest {
    String goodFilePath = "logback-test.xml";

    @BeforeEach
    void setup() {
        FileUtils.copyFile(new File("src/test/resources/logback-test.xml"), new File(goodFilePath));
    }

    @AfterEach
    void teardown() {
        FileUtils.delete(new File(goodFilePath));
    }

    @Test
    @Tag("regressiontest")
    void testLoadingConfig() throws Exception {
        addDescription("Test the loading of a configuration file for the config loader.");
        addStep("Setup variables", "");
        String badFilePath = "iDoNotExist.xml";
        Assertions.assertFalse(new File(badFilePath).exists());
        Assertions.assertTrue(new File(goodFilePath).exists());

        addStep("Test with a invalid file path", "Should throw an exception");
        try {
            new LogbackConfigLoader(badFilePath);
            Assertions.fail("Should throw an exception");
        } catch (IllegalArgumentException e) {
            // expected
        }

        addStep("Test when the file is unreadable", "Should throw an exception");
        File goodFile = new File(goodFilePath);
        try {
            goodFile.setReadable(false);

            try {
                new LogbackConfigLoader(goodFilePath);
                Assertions.fail("Should throw an exception");
            } catch (IllegalArgumentException e) {
                // expected
            }
        } finally {
            goodFile.setReadable(true);
            goodFile.setExecutable(true);
            goodFile.setWritable(true);
        }

        addStep("success case", "Should not throw an exception");
        new LogbackConfigLoader(goodFilePath);
    }

}
