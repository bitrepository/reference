package org.bitrepository.pillar;

import org.junit.platform.suite.api.*;

/**
 * BitrepositoryPillarTestSuite is a JUnit 5 suite class that groups and configures multiple test classes
 * for the BitRepositoryPillar project.
 */
@Suite
@SuiteDisplayName("Full Pillar Acceptance Test")
@SelectPackages({"org.bitrepository.pillar.integration.func"})
@IncludeClassNamePatterns(value = "^(Test.*|.+[.$]Test.*|.*Tests?|.*IT)$")
@IncludeTags({PillarTestGroups.FULL_PILLAR_TEST})
@ConfigurationParameter(key = "pillarType", value = "File")
public class BitrepositoryPillarTestSuite {
}
