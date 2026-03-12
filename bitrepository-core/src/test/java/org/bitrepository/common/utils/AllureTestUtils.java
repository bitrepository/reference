package org.bitrepository.common.utils;

import io.qameta.allure.Allure;

import java.util.NoSuchElementException;

public class AllureTestUtils {

    /**
     * Check if we're inside an active test context
     */
    public static boolean isTestRunning() {
        try {
            Allure.getLifecycle().getCurrentTestCase();
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    /**
     * Add a description to the current test
     */
    public static void addDescription(String description) {
        if (isTestRunning()) {
            Allure.description(description);
        }
    }

    /**
     * Add a test step with expected result
     */
    public static void addStep(String stepDescription, String expectedResult) {
        if (!isTestRunning())
        {
            return;
        }
        Allure.step(stepDescription, () -> 
            Allure.addAttachment("Expected Result", "text/plain", expectedResult));
    }

    /**
     * Add a fixture/setup description
     */
    public static void addFixture(String fixtureDescription) {
        if (!isTestRunning()) return;
        Allure.step("Fixture: " + fixtureDescription, () -> Allure.step("Setup: " + fixtureDescription));
    }

    /**
     * Add a reference
     */
    public static void addReference(String reference) {
        if (!isTestRunning()) return;
        if (reference.contains("BITMAG-")) {
            int startIdx = reference.indexOf("BITMAG-");
            int endIdx = reference.indexOf(">", startIdx);
            if (endIdx != -1) {
                String jiraIssue = reference.substring(startIdx, endIdx);
                Allure.link(jiraIssue, "https://sbforge.org/jira/browse/" + jiraIssue);
            }
        }
        Allure.addAttachment("Reference", "text/html", reference);
    }
}
