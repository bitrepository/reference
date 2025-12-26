package org.bitrepository.pillar;

import org.bitrepository.protocol.GlobalSuiteExtension;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.bus.ActiveMQMessageBusTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * BitrepositoryPillarTestSuite is a JUnit 5 suite class that groups and configures multiple test classes
 * for the BitRepositoryPillar project. This suite uses JUnit 5 annotations to select test classes, packages,
 * and tags, and extend the suite with custom extensions.
 *
 * <p>JUnit 5 Annotations Used:</p>
 * <ul>
 *     <li>{@link Suite}: Indicates that this class is a JUnit 5 suite. It groups multiple test classes
 *     into a single test suite.</li>
 *     <li>{@link SelectClasses}: Specifies the test classes to be included in the suite. The value is an array
 *     of class references to the test classes.</li>
 *     <li>{@link SelectPackages}: Specifies the test packages to be included in the suite. The value is an array
 *     of package names.</li>
 *     <li>{@link IncludeTags}: Specifies the tags to include in the suite. The value is an array of tag names.</li>
 *     <li>{@link ExcludeTags}: Specifies the tags to exclude from the suite. The value is an array of tag names.</li>
 *     <li>{@link ExtendWith}: Specifies the extensions to be applied to the suite. The value is an array of
 *     extension classes.</li>
 * </ul>
 *
 * <p>Options in a JUnit 5 Suite:</p>
 * <ul>
 *     <li><strong>Selecting Test Classes:</strong> Use the {@link SelectClasses} annotation to specify the test
 *     classes to be included in the suite. The value is an array of class references to the test classes.</li>
 *     <li><strong>Selecting Test Packages:</strong> Use the {@link SelectPackages} annotation to specify the test
 *     packages to be included in the suite. The value is an array of package names.</li>
 *     <li><strong>Selecting Tests by Tag:</strong> Use the {@link IncludeTags} and {@link ExcludeTags} annotations
 *     to specify the tags to include or exclude in the suite. The value is an array of tag names.</li>
 *     <li><strong>Extending the Suite:</strong> Use the {@link ExtendWith} annotation to specify custom extensions
 *     to be applied to the suite. The value is an array of extension classes.</li>
 * </ul>
 *
 * <p>Example Usage:</p>
 * <pre>
 * {@code
 * @Suite
 * @SelectClasses({BitrepositoryPillarTest.class})  // List your test classes here
 * @SelectPackages("org.bitrepository.pillar")  // List your test packages here
 * @IncludeTags("integration")  // List your include tags here
 * @ExcludeTags("slow")  // List your exclude tags here
 * @ExtendWith(GlobalSuiteExtension.class)
 * public class BitrepositoryTestSuite {
 *     // No need for methods here; this just groups and extends
 * }
 * }
 * </pre>
 */
@Suite
@SelectClasses({IntegrationTest.class, ActiveMQMessageBusTest.class})  // List your test classes here
@ExtendWith(GlobalSuiteExtension.class)
public class BitrepositoryPillarTestSuite {
}
