package org.bitrepository.common.utils;

import io.qameta.allure.Allure;

public class AllureTestUtils {

    /**
     * Check if we're inside an active test context
     */
    public static boolean isTestRunning() {
        try {
            Allure.getLifecycle().getCurrentTestCase();
            return true;
        } catch (IllegalStateException e) {
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
        if (!isTestRunning()) return;
        Allure.step(stepDescription, () -> {
            Allure.addAttachment("Expected Result", "text/plain", expectedResult);
        });
    }

    /**
     * Add a fixture/setup description
     */
    public static void addFixture(String fixtureDescription) {
        if (!isTestRunning()) return;
        Allure.step("Fixture: " + fixtureDescription, () -> {
            Allure.step("Setup: " + fixtureDescription);
        });
    }

    /**
     * Add a reference
     */
    public static void addReference(String reference) {
        if (!isTestRunning()) return;
        if (reference.contains("BITMAG-")) {
            int startIdx = reference.indexOf("BITMAG-");
            int endIdx = reference.indexOf(">", startIdx);
            if (endIdx > startIdx) {
                String jiraIssue = reference.substring(startIdx, endIdx);
                Allure.link(jiraIssue, "https://sbforge.org/jira/browse/" + jiraIssue);
            }
        }
        Allure.addAttachment("Reference", "text/html", reference);
    }

    /**
     * Add a step that executes code
     */
    public static <T> T addStep(String stepDescription, StepBody<T> body) {
        handleStepRunning(body);
        return Allure.step(stepDescription, body::execute);
    }

    private static <T> void handleStepRunning(StepBody<T> body) {
        if (!isTestRunning()) {
            try {
                body.execute();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Add a step that executes code with expected result documentation
     */
    public static <T> T addStep(String stepDescription, String expectedResult, StepBody<T> body) {
        handleStepRunning(body);
        return Allure.step(stepDescription, () -> {
            Allure.addAttachment("Expected Result", "text/plain", expectedResult);
            return body.execute();
        });
    }

    /**
     * Functional interface for steps that return a value
     */
    @FunctionalInterface
    public interface StepBody<T> {
        T execute() throws Exception;
    }

    /**
     * Functional interface for steps that don't return a value
     */
    @FunctionalInterface
    public interface VoidStepBody {
        void execute() throws Exception;
    }
}
