package org.bitrepository.protocol.utils;

import io.qameta.allure.Allure;
import io.qameta.allure.Step;

public class AllureTestUtils {
    /**
     * Add a description to the current test
     */
    public static void addDescription(String description) {
        Allure.description(description);
    }

    /**
     * Add a test step with expected result
     */
    @Step("{stepDescription}")
    public static void addStep(String stepDescription, String expectedResult) {
        Allure.step(stepDescription, () -> {
            Allure.addAttachment("Expected Result", "text/plain", expectedResult);
        });
    }

    /**
     * Add a fixture/setup description
     */
    @Step("Fixture: {fixtureDescription}")
    public static void addFixture(String fixtureDescription) {
        Allure.step("Setup: " + fixtureDescription);
    }

    /**
     * Add a reference (typically to JIRA issues)
     */
    public static void addReference(String reference) {
        // Extract JIRA issue if present
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
    @Step("{stepDescription}")
    public static <T> T addStep(String stepDescription, StepBody<T> body) {
        return Allure.step(stepDescription, body::execute);
    }

    /**
     * Add a step that executes code with expected result documentation
     */
    @Step("{stepDescription}")
    public static <T> T addStep(String stepDescription, String expectedResult, StepBody<T> body) {
        return Allure.step(stepDescription, () -> {
            Allure.addAttachment("Expected Result", "text/plain", expectedResult);
            return body.execute();
        });
    }

    /**
     * Add a step that executes code without return value
     */
    @Step("{stepDescription}")
    public static void addStepVoid(String stepDescription, VoidStepBody body) {
        Allure.step(stepDescription, () -> {
            body.execute();
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
