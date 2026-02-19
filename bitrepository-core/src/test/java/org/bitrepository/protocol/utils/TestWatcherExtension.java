package org.bitrepository.protocol.utils;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

public class TestWatcherExtension implements TestWatcher {
    private boolean testSuccessful = true;

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        testSuccessful = false;
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        testSuccessful = false;
    }

    public boolean isTestSuccessful() {
        return testSuccessful;
    }
}