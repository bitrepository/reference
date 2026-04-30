package org.bitrepository.pillar;

import org.junit.platform.suite.api.ExcludePackages;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * BitrepositoryPillarTestSuite is a JUnit 5 suite class that groups and configures multiple test classes
 * for the BitRepositoryPillar project.
 */
@Suite
@SuiteDisplayName("Common Acceptance Test")
@SelectPackages({"org.bitrepository.pillar"})
@ExcludePackages("org.bitrepository.pillar.integration")
@IncludeClassNamePatterns(value = {"^(.*IT)$"})
public class BitrepositoryPillarCommonIntegrationTestSuite {
}
