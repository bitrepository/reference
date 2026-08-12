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
package org.bitrepository.commandline;

import org.bitrepository.SuiteInfoParameterResolver;
import org.bitrepository.TestGroups;
import org.bitrepository.client.DefaultFixtureClientIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Instant;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SuiteInfoParameterResolver.class)
public class ReplaceFileCmdIT extends DefaultFixtureClientIT {
    private static final String SETTINGS_DIR = "settings/xml/bitrepository-devel";
    private static final String KEY_FILE = "KeyFile";
    private static final String DEFAULT_CHECKSUM = "0123456789";

    private String DEFAULT_COLLECTION_ID;

    @BeforeEach
    public void setupClient() {
        DEFAULT_COLLECTION_ID = settingsForTestClient.getCollections().get(0).getID();
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void defaultSuccessScenarioTest() {
        addDescription("Tests simplest arguments for running the CmdLineClient");
        String[] args = new String[]{"-s" + SETTINGS_DIR,
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-f" + DEFAULT_FILE_ID,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID};
        new ReplaceFileCmd(args);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void URLSuccessScenarioTest() {
        addDescription("Tests the scenario, where a URL instead of a file is used for the replacement file.");
        String[] args = new String[]{"-s" + SETTINGS_DIR,
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-u" + defaultDownloadFileAddress,
                "-r" + DEFAULT_CHECKSUM,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID,
                "-i" + DEFAULT_FILE_ID};
        new ReplaceFileCmd(args);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void missingCollectionArgumentTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            addDescription("Tests the scenario, where the collection arguments is missing.");
            String[] args = new String[]{"-s" + SETTINGS_DIR,
                    "-k" + KEY_FILE,
                    "-p" + PILLAR1_ID,
                    "-u" + defaultDownloadFileAddress,
                    "-r" + DEFAULT_CHECKSUM,
                    "-C" + DEFAULT_CHECKSUM,
                    "-i" + DEFAULT_FILE_ID};
            new ReplaceFileCmd(args);
        });
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void missingPillarArgumentTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            addDescription("Tests the different scenarios, with the pillar argument.");
            String[] args = new String[]{"-s" + SETTINGS_DIR,
                    "-k" + KEY_FILE,
                    "-u" + defaultDownloadFileAddress,
                    "-r" + DEFAULT_CHECKSUM,
                    "-C" + DEFAULT_CHECKSUM,
                    "-c" + DEFAULT_COLLECTION_ID,
                    "-i" + DEFAULT_FILE_ID};
            new ReplaceFileCmd(args);
        });
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void unknownPillarArgumentTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            addStep("Testing against a non-existing pillar id", "Should fail");
            String[] args = new String[]{"-s" + SETTINGS_DIR,
                    "-k" + KEY_FILE,
                    "-u" + defaultDownloadFileAddress,
                    "-r" + DEFAULT_CHECKSUM,
                    "-C" + DEFAULT_CHECKSUM,
                    "-c" + DEFAULT_COLLECTION_ID,
                    "-p" + "Random" + Instant.now().toEpochMilli() + "pillar",
                    "-i" + DEFAULT_FILE_ID};
            new ReplaceFileCmd(args);
        });
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void missingFileOrURLArgumentTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            addDescription("Tests the scenario, where no arguments for file or url is given.");
            String[] args = new String[]{"-s" + SETTINGS_DIR,
                    "-k" + KEY_FILE,
                    "-p" + PILLAR1_ID,
                    "-r" + DEFAULT_CHECKSUM,
                    "-c" + DEFAULT_COLLECTION_ID,
                    "-C" + DEFAULT_CHECKSUM,
                    "-i" + DEFAULT_FILE_ID};
            new ReplaceFileCmd(args);
        });
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void bothFileAndURLArgumentTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            addDescription("Tests the scenario, where both arguments for file or url is given.");
            String[] args = new String[]{"-s" + SETTINGS_DIR,
                    "-k" + KEY_FILE,
                    "-p" + PILLAR1_ID,
                    "-r" + DEFAULT_CHECKSUM,
                    "-f" + DEFAULT_FILE_ID,
                    "-u" + defaultDownloadFileAddress,
                    "-c" + DEFAULT_COLLECTION_ID,
                    "-C" + DEFAULT_CHECKSUM,
                    "-i" + DEFAULT_FILE_ID};
            new ReplaceFileCmd(args);
        });
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void missingFileIDWhenURLArgumentTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            addDescription("Tests the scenario, where no checksum argument is given, but a URL is given.");
            String[] args = new String[]{"-s" + SETTINGS_DIR,
                    "-k" + KEY_FILE,
                    "-p" + PILLAR1_ID,
                    "-u" + defaultDownloadFileAddress,
                    "-r" + DEFAULT_CHECKSUM,
                    "-c" + DEFAULT_COLLECTION_ID,
                    "-C" + DEFAULT_CHECKSUM};
            new ReplaceFileCmd(args);
        });
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void missingFileIDWhenFileArgumentTest() {
        addDescription("Tests the scenario, where no checksum argument is given, but a URL is given.");
        String[] args = new String[]{"-s" + SETTINGS_DIR,
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-f" + DEFAULT_FILE_ID,
                "-c" + DEFAULT_COLLECTION_ID,
                "-C" + DEFAULT_CHECKSUM};
        new ReplaceFileCmd(args);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void missingChecksumForNewFileWhenUsingURLArgumentTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            addDescription("Tests the scenario, where no checksum argument is given, but a URL is given.");
            String[] args = new String[]{"-s" + SETTINGS_DIR,
                    "-k" + KEY_FILE,
                    "-p" + PILLAR1_ID,
                    "-u" + defaultDownloadFileAddress,
                    "-C" + DEFAULT_CHECKSUM,
                    "-c" + DEFAULT_COLLECTION_ID,
                    "-i" + DEFAULT_FILE_ID};
            new ReplaceFileCmd(args);
        });
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void missingChecksumForNewFileWhenUsingFileArgumentTest() {
        addDescription("Tests the scenario, where no checksum argument is given, but a File is given.");
        String[] args = new String[]{"-s" + SETTINGS_DIR,
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-f" + DEFAULT_FILE_ID,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID,
                "-i" + DEFAULT_FILE_ID};
        new ReplaceFileCmd(args);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void missingChecksumForExistingFileWhenUsingURLArgumentTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            addDescription("Tests the scenario, where no checksum argument is given, but a URL is given.");
            String[] args = new String[]{"-s" + SETTINGS_DIR,
                    "-k" + KEY_FILE,
                    "-p" + PILLAR1_ID,
                    "-u" + defaultDownloadFileAddress,
                    "-r" + DEFAULT_CHECKSUM,
                    "-c" + DEFAULT_COLLECTION_ID,
                    "-i" + DEFAULT_FILE_ID};
            new ReplaceFileCmd(args);
        });
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void missingChecksumForExistingFileWhenUsingFileArgumentTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            addDescription("Tests the scenario, where no checksum argument is given, but a File is given.");
            String[] args = new String[]{"-s" + SETTINGS_DIR,
                    "-k" + KEY_FILE,
                    "-p" + PILLAR1_ID,
                    "-f" + DEFAULT_FILE_ID,
                    "-r" + DEFAULT_CHECKSUM,
                    "-c" + DEFAULT_COLLECTION_ID,
                    "-i" + DEFAULT_FILE_ID};
            new ReplaceFileCmd(args);
        });
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void checksumArgumentNonSaltAlgorithmWitoutSaltTest() {
        addDescription("Test MD5 checksum without salt -> no failure");
        String[] args = new String[]{"-s" + SETTINGS_DIR,
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-u" + defaultDownloadFileAddress,
                "-r" + DEFAULT_CHECKSUM,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID,
                "-i" + DEFAULT_FILE_ID,
                "-R" + "MD5"};
        new ReplaceFileCmd(args);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public void checksumArgumentSaltAlgorithmWithSaltTest() {
        addDescription("Test HMAC_SHA256 checksum with salt -> No failure");
        String[] args = new String[]{"-s" + SETTINGS_DIR,
                "-k" + KEY_FILE,
                "-p" + PILLAR1_ID,
                "-u" + defaultDownloadFileAddress,
                "-r" + DEFAULT_CHECKSUM,
                "-C" + DEFAULT_CHECKSUM,
                "-c" + DEFAULT_COLLECTION_ID,
                "-i" + DEFAULT_FILE_ID,
                "-R" + "HMAC_SHA256",
                "-S" + "SALT"};
        new ReplaceFileCmd(args);
    }
}
