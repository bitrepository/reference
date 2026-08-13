package org.bitrepository.protocol;

import org.bitrepository.TestGroups;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * BitrepositoryTestSuite is a JUnit 5 suite class that groups and configures multiple test classes
 * for the BitRepository project.
 */
@Suite
@SuiteDisplayName("Stress Test")
@SelectPackages({"org.bitrepository.protocol"})
@IncludeTags(TestGroups.STRESS_TEST)
public class BitrepositoryPerformanceTestSuite {
    // No need for methods here; this just groups and extends
}
