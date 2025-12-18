package org.bitrepository.protocol.utils;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.util.Optional;

public class TestWatcherExtension implements TestWatcher {
    private static boolean testSuccessful = true;

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        testSuccessful = false;
    }

    @Override
    public void testDisabled(ExtensionContext context, Optional<String> reason) {
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        testSuccessful = false;
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        testSuccessful = true;
    }

    public static boolean isTestSuccessful() {
        return testSuccessful;
    }
}