package org.bitrepository.pillar;

import org.junit.platform.suite.api.*;

/**
 * BitrepositoryPerformanceTestSuite is a JUnit 5 suite class that groups and configures multiple test classes
 * for the BitRepositoryPillar project.
 */
@Suite
@SuiteDisplayName("Performance Test")
@SelectPackages({"org.bitrepository.pillar.integration.perf"})
@IncludeClassNamePatterns(value = "^(Test.*|.+[.$]Test.*|.*Tests?|.*IT)$")
@IncludeTags("pillar-stress-test")
@ConfigurationParameter(key = "pillarType", value = "File")
public class BitrepositoryPerformanceTestSuite {
}
