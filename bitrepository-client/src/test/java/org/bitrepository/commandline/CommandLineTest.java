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
import org.bitrepository.commandline.utils.CommandLineArgumentsHandler;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class CommandLineTest extends ExtendedTestCase {
    private static final String SETTINGS_DIR = "SettingsDir";
    private static final String KEY_FILE = "KeyFile";
    private static final String DUMMY_DATA = "DummyData";

    @Test(groups = { "regressiontest" }, expectedExceptions = Exception.class)
    public void argumentsTesterUnknownArgument() throws Exception {
        addDescription("Test the handling of arguments by the CommandLineArgumentHandler.");
        CommandLineArgumentsHandler clah = new CommandLineArgumentsHandler();

        addStep("Validate arguments without any options.", "Ok, when no arguments, but fails when arguments given.");
        clah.parseArguments(new String[0]);

        clah.parseArguments("-Xunknown...");
    }

    @Test(groups = { "regressiontest" }, expectedExceptions = Exception.class)
    public void argumentsTesterWrongArgument() throws Exception {
        addDescription("Test the handling of arguments by the CommandLineArgumentHandler.");
        CommandLineArgumentsHandler clah = new CommandLineArgumentsHandler();

        addStep("Validate the default options", "Ok, when both given. Fails if either is missing");
        clah = new CommandLineArgumentsHandler();
        clah.createDefaultOptions();
        clah.parseArguments("-s" + SETTINGS_DIR, "-k" + KEY_FILE);
        assertEquals(clah.getOptionValue("s"), SETTINGS_DIR);
        assertEquals(clah.getOptionValue("k"), KEY_FILE);

        clah = new CommandLineArgumentsHandler();
        clah.createDefaultOptions();
        clah.parseArguments();
    }

    @Test(groups = { "regressiontest" })
    public void newArgumentTester() throws Exception {
        addDescription("Test the handling of a new argument.");
        CommandLineArgumentsHandler clah = new CommandLineArgumentsHandler();
        String argName = "X";
        Option newOption = new Option(argName, true, "Test argument.");
        clah.addOption(newOption);

        addStep("Test the option", "Works");
        clah.parseArguments("-" + argName + DUMMY_DATA);

        assertTrue(clah.hasOption(argName));
        assertEquals(clah.getOptionValue(argName), DUMMY_DATA);

        assertNotNull(clah.listArguments());
    }
}
