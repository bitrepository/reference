package org.bitrepository.pillar.integration;

import org.bitrepository.pillar.PillarTestGroups;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.platform.suite.api.*;

/**
 * BitrepositoryPerformanceTestSuite is a JUnit 5 suite class that groups and configures multiple test classes
 * for the BitRepositoryPillar project.
 */
@Suite
@SuiteDisplayName("Performance Test")
@SelectPackages({"org.bitrepository.pillar.integration.perf"})
@IncludeClassNamePatterns(value = {"^(Test.*)$",
                                   "^(.+[.$]Test.*)$",
                                   "^(.*Tests?)$",
                                   "^(.*IT)$"
})
@IncludeTags(PillarTestGroups.PILLAR_STRESS_TEST)
@ConfigurationParameter(key = "pillarType", value = "File")
@EnabledIfSystemProperty(named = "runStressTests", matches = "true")
public class BitrepositoryPerformanceTestSuite {
}
