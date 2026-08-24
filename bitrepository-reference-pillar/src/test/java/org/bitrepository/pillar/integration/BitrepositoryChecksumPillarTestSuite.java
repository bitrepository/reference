package org.bitrepository.pillar.integration;

import org.bitrepository.pillar.PillarTestGroups;
import org.junit.platform.suite.api.*;

/**
 * BitrepositoryPillarTestSuite is a JUnit 5 suite class that groups and configures multiple test classes
 * for the BitRepositoryPillar project.
 */
@Suite(failIfNoTests = true)
@SuiteDisplayName("Checksum Pillar Acceptance Test")
@SelectPackages({"org.bitrepository.pillar.integration.func"})
@IncludeTags({PillarTestGroups.CHECKSUM_PILLAR_TEST})
@IncludeClassNamePatterns(value = {"^(Test.*)$",
                                   "^(.+[.$]Test.*)$",
                                   "^(.*Tests?)$",
                                   "^(.*IT)$"
})
@ConfigurationParameter(key = "pillarType", value = "Checksum")
public class BitrepositoryChecksumPillarTestSuite {
}
