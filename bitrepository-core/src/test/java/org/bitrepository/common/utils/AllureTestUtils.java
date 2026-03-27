package org.bitrepository.common.utils;

import io.qameta.allure.Allure;

public class AllureTestUtils {

    /**
     * Check if we're inside an active test context
     */
    public static boolean isTestRunning() {
        return Allure.getLifecycle().getCurrentTestCase().isPresent();
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
        
        // If the string does not start with a tag (e.g. <ol>, <p>), assume it is plain text 
        // and preserve newlines by converting them to HTML line breaks.
        String content = expectedResult.trim().startsWith("<") 
                ? expectedResult 
                : expectedResult.replace("\n", "<br>");

        Allure.step(stepDescription, () -> 
            Allure.addAttachment("Expected Result", "text/html", content));
    }

    /**
     * Add a fixture/setup description
     */
    public static void addFixture(String fixtureDescription) {
        if (!isTestRunning()) {
            return;
        }
        Allure.step("Fixture: " + fixtureDescription, () -> Allure.step("Setup: " + fixtureDescription));
    }

    /**
     * Add a reference, with a default link for BITMAG issues.
     * @param reference The reference text to attach.
     */
    public static void addReference(String reference) {
        addReference(reference, "BITMAG-", "https://sbforge.org/jira/browse/");
    }

    /**
     * Add a reference as an attachment. If the reference text contains a specific prefix,
     * it also creates a URL link for it.
     *
     * @param reference The reference text to attach.
     * @param linkPrefix The prefix to search for in the reference text, e.g., "BITMAG-". If null or empty, no link will be created.
     * @param linkBaseUrl The base URL for the link. The found ID will be appended to this URL. If null, no link will be created.
     */
    public static void addReference(String reference, String linkPrefix, String linkBaseUrl) {
        if (!isTestRunning()) {
            return;
        }
        if (linkPrefix != null && !linkPrefix.isEmpty() && reference.contains(linkPrefix)) {
            int startIdx = reference.indexOf(linkPrefix);
            int endIdx = reference.indexOf(">", startIdx);
            if (endIdx != -1) {
                String linkId = reference.substring(startIdx, endIdx);
                if (linkBaseUrl != null) {
                    Allure.link(linkId, linkBaseUrl + linkId);
                }
            }
        }
        Allure.addAttachment("Reference", "text/html", reference);
    }
}
