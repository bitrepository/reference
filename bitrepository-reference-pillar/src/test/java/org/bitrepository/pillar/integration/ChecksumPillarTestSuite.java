package org.bitrepository.pillar.integration;

import org.bitrepository.pillar.PillarTestGroups;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * JUnit 5 suite entry-point for the Checksum Pillar acceptance tests.
 *
 * <p>Direct replacement for the TestNG suite XML:
 * <pre>{@code
 * <suite name="Checksum Pillar Acceptance Test">
 *   <test name="Checksum Pillar Test">
 *     <groups><run><include name="checksumPillarTest"/></run></groups>
 *     <packages>
 *       <package name="org.bitrepository.pillar.integration.func.*"/>
 *     </packages>
 *   </test>
 * </suite>
 * }</pre>
 *
 * <h3>How to run</h3>
 * <ul>
 *   <li><b>Maven:</b>
 *     {@code mvn test -Dtest=ChecksumPillarTestSuite -Dbitrepository.pillar.type=checksum}
 *   </li>
 *   <li><b>IDE:</b> Run this class directly as a JUnit 5 suite.
 *     Set the system property {@code bitrepository.pillar.type=checksum} in the
 *     run configuration.
 *   </li>
 * </ul>
 *
 * <h3>Pillar type</h3>
 * The system property {@code bitrepository.pillar.type} tells {@link PillarSuiteExtension}
 * whether to start a checksum or reference pillar.  This suite defaults to {@code checksum}.
 * If you need a reference-pillar suite, create a sibling class and set the property to
 * {@code reference}.
 */
@Suite
@SuiteDisplayName("Checksum Pillar Acceptance Test")
@SelectPackages({
        "org.bitrepository.pillar.messagehandling",
        "org.bitrepository.pillar.integration"
})
@IncludeTags({"regressiontest", PillarTestGroups.CHECKSUM_PILLAR_TEST})
@ExtendWith(PillarSuiteExtension.class)
public class ChecksumPillarTestSuite {
    // Suite classes are purely declarative – no code here.
}
