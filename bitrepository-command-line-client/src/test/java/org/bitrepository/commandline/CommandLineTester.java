package org.bitrepository.commandline;

import org.apache.commons.cli.Option;
import org.bitrepository.commandline.utils.CommandLineArgumentsHandler;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CommandLineTester extends ExtendedTestCase {
    private static final String SETTINGS_DIR = "SettingsDir";
    private static final String KEY_FILE = "KeyFile";
    private static final String DUMMY_DATA = "DummyData";
    
    @Test(groups = { "regressiontest" })
    public void argumentsTester() throws Exception {
        addDescription("Test the handling of arguments by the CommandLineArgumentHandler.");
        CommandLineArgumentsHandler clah = new CommandLineArgumentsHandler();
        
        addStep("Validate arguments without any options.", "Ok, when no arguments, but fails when arguments given.");
        clah.parseArguments(new String[0]);
        
        try {
            clah.parseArguments("-Xunknown...");
            Assert.fail("Should fail.");
        } catch (Exception e) {
            // expected.
        }
        
        addStep("Validate the default options", "Ok, when both given. Fails if either is missing");
        clah = new CommandLineArgumentsHandler();
        clah.createDefaultOptions();
        clah.parseArguments("-s" + SETTINGS_DIR, "-k" + KEY_FILE);
        Assert.assertEquals(clah.getOptionValue("s"), SETTINGS_DIR);
        Assert.assertEquals(clah.getOptionValue("k"), KEY_FILE);
        
        clah = new CommandLineArgumentsHandler();
        clah.createDefaultOptions();
        try {
            clah.parseArguments("-s" + SETTINGS_DIR);
            Assert.fail("Should throw exception, since missing argument.");
        } catch(Exception e) {
            // expected
        }
        
        clah = new CommandLineArgumentsHandler();
        clah.createDefaultOptions();
        try {
            clah.parseArguments("-k" + KEY_FILE);
            Assert.fail("Should throw exception, since missing argument.");
        } catch(Exception e) {
            // expected
        }
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
        
        Assert.assertTrue(clah.hasOption(argName));
        Assert.assertEquals(clah.getOptionValue(argName), DUMMY_DATA);
        
        Assert.assertNotNull(clah.listArguments());
    }
}
