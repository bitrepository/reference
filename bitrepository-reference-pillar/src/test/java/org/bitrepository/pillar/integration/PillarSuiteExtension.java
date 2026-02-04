package org.bitrepository.pillar.integration;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class PillarSuiteExtension implements BeforeAllCallback {

    /**
     * System property that selects the pillar type.
     */
    public static final String PILLAR_TYPE_PROPERTY = "bitrepository.pillar.type";

    private static final String CHECKSUM = "checksum";

    /**
     * The pillar instance (null until first test class registers it).
     */
    private static volatile EmbeddedPillar embeddedPillar = null;

    /**
     * Guard to ensure shutdown hook is only registered once.
     */
    private static volatile boolean shutdownHookRegistered = false;

    /**
     * Lock for synchronized access.
     */
    private static final Object LOCK = new Object();

    @Override
    public void beforeAll(ExtensionContext context) {
        // Extension hook – currently unused; registration happens via static method.
    }

    /**
     * Returns {@code true} if a pillar has already been started.
     * {@link PillarIntegrationTest} calls this to decide whether to start a new one.
     */
    public static boolean isPillarAlreadyStarted() {
        return embeddedPillar != null;
    }

    /**
     * Registers the given {@link EmbeddedPillar} and ensures it will be shut down
     * when the JVM exits.
     *
     * <p>This must be called exactly once, from the first test class's {@code @BeforeAll}.
     */
    public static void registerPillar(EmbeddedPillar pillar) {
        synchronized (LOCK) {
            if (embeddedPillar != null) {
                throw new IllegalStateException("Pillar already registered");
            }
            embeddedPillar = pillar;

            // Register shutdown hook to ensure pillar is stopped even if tests fail
            if (!shutdownHookRegistered) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (embeddedPillar != null) {
                        embeddedPillar.shutdown();
                    }
                }, "pillar-shutdown"));
                shutdownHookRegistered = true;
            }
        }
    }

    /**
     * Returns {@code true} when the suite should use a checksum pillar.
     * <p>
     * {@code testContext.getIncludedGroups().contains("checksumPillarTest")}.
     */
    public static boolean isChecksumPillar() {
        String type = System.getProperty(PILLAR_TYPE_PROPERTY, CHECKSUM);
        return CHECKSUM.equalsIgnoreCase(type);
    }
}
