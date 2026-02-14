package org.bitrepository.protocol;

import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * BitrepositoryTestSuite is a JUnit 5 suite class that groups and configures multiple test classes
 * for the BitRepository project.
 */
@Suite
@SelectPackages("org.bitrepository.protocol")
@IncludeTags("regressiontest")
public class BitrepositoryTestSuite {
    // No need for methods here; this just groups and extends
}
