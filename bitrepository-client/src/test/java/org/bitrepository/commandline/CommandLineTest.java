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

import org.apache.commons.cli.Option;
import org.bitrepository.TestGroups;
import org.bitrepository.commandline.utils.CommandLineArgumentsHandler;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class CommandLineTest {
    private static final String SETTINGS_DIR = "SettingsDir";
    private static final String KEY_FILE = "KeyFile";
    private static final String DUMMY_DATA = "DummyData";

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void argumentsTesterUnknownArgument() throws Exception {
        assertThrows(Exception.class, () -> {
            addDescription("Test the handling of arguments by the CommandLineArgumentHandler.");
            CommandLineArgumentsHandler clah = new CommandLineArgumentsHandler();

            addStep("Validate arguments without any options.",
                    "Ok, when no arguments, but fails when arguments given.");
            clah.parseArguments();

            clah.parseArguments("-Xunknown...");
        });
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void argumentsTesterWrongArgument() throws Exception {
        assertThrows(Exception.class, () -> {
            addDescription("Test the handling of arguments by the CommandLineArgumentHandler.");
            CommandLineArgumentsHandler clah = new CommandLineArgumentsHandler();

            addStep("Validate the default options", "Ok, when both given. Fails if either is missing");
            clah = new CommandLineArgumentsHandler();
            clah.createDefaultOptions();
            clah.parseArguments("-s" + SETTINGS_DIR, "-k" + KEY_FILE);
            assertEquals(SETTINGS_DIR, clah.getOptionValue("s"));
            assertEquals(KEY_FILE, clah.getOptionValue("k"));

            clah = new CommandLineArgumentsHandler();
            clah.createDefaultOptions();
            clah.parseArguments();
        });
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void newArgumentTester() throws Exception {
        addDescription("Test the handling of a new argument.");
        CommandLineArgumentsHandler clah = new CommandLineArgumentsHandler();
        String argName = "X";
        Option newOption = new Option(argName, true, "Test argument.");
        clah.addOption(newOption);

        addStep("Test the option", "Works");
        clah.parseArguments("-" + argName + DUMMY_DATA);

        assertTrue(clah.hasOption(argName));
        assertEquals(DUMMY_DATA, clah.getOptionValue(argName));

        assertNotNull(clah.listArguments());
    }
}
