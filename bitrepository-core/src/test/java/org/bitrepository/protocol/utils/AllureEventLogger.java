package org.bitrepository.protocol.utils;

import com.google.gson.Gson;
import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class AllureEventLogger {
    private final List<BitrepositoryEvent> events = new CopyOnWriteArrayList<>();
    private final String componentName;
    private static final Gson gson = new Gson();

    public AllureEventLogger(String componentName) {
        this.componentName = componentName;
        allureStep("Initialize event logger for: " + componentName);
    }

    /**
     * Check if we're inside an active test context
     */
    private boolean isTestRunning() {
        try {
            Allure.getLifecycle().getCurrentTestCase();
            return true;
        } catch (IllegalStateException e) {
            return false;
        }
    }

    /**
     * Safely execute Allure step only if test context is active
     */
    private void allureStep(String stepName) {
        if (isTestRunning()) {
            Allure.step(stepName);
        }
    }

    /**
     * Safely execute Allure step with status only if test context is active
     */
    private void allureStep(String stepName, Status status) {
        if (isTestRunning()) {
            Allure.step(stepName, status);
        }
    }

    /**
     * Safely add attachment only if test context is active
     */
    private void allureAttachment(String name, String type, String content, String fileExtension) {
        if (isTestRunning()) {
            Allure.addAttachment(name, type, content, fileExtension);
        }
    }

    /**
     * Safely add attachment only if test context is active
     */
    private void allureAttachment(String name, String type, String content) {
        if (isTestRunning()) {
            Allure.addAttachment(name, type, content);
        }
    }

    /**
     * Log an event (replaces TestEventHandler's event capturing)
     */
    public void logEvent(BitrepositoryEvent event) {
        allureStep("Event received: " + event.getType());
        events.add(event);

        allureAttachment(
                String.format(Locale.ROOT, "[%s] Event: %s", componentName, event.getType()),
                "application/json",
                gson.toJson(Map.of(
                        "type", event.getType(),
                        "timestamp", event.getTimestamp(),
                        "data", event.getData()
                )),
                ".json"
        );
    }

    /**
     * Wait for a specific event (replaces TestEventHandler.waitForEvent)
     */
    public BitrepositoryEvent waitForEvent(String eventType, Duration timeout) {
        allureStep(String.format(Locale.ROOT, "Waiting for event: %s (timeout: %ds)", eventType, timeout.getSeconds()));
        Instant deadline = Instant.now().plus(timeout);

        while (Instant.now().isBefore(deadline)) {
            BitrepositoryEvent event = findEvent(eventType);
            if (event != null) {
                allureStep(
                        String.format(Locale.ROOT, "✓ Event '%s' received after %dms",
                                eventType,
                                Duration.between(event.getTimestamp(), Instant.now()).toMillis()),
                        Status.PASSED
                );
                return event;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for event", e);
            }
        }

        // Event not received - fail with detailed info
        String capturedEvents = events.isEmpty()
                ? "No events captured"
                : "Captured events: " + events.stream()
                .map(BitrepositoryEvent::getType)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        allureStep(
                String.format(Locale.ROOT, "✗ Timeout waiting for '%s'. %s", eventType, capturedEvents),
                Status.FAILED
        );

        throw new AssertionError(
                String.format(Locale.ROOT, "Event '%s' not received within %ds. %s",
                        eventType, timeout.getSeconds(), capturedEvents)
        );
    }

    /**
     * Check if event was received (replaces TestEventHandler.receivedEvent)
     */
    public boolean receivedEvent(String eventType) {
        allureStep("Verify event received: " + eventType);
        boolean received = findEvent(eventType) != null;

        if (received) {
            allureStep("✓ Event '" + eventType + "' was received", Status.PASSED);
        } else {
            allureStep("✗ Event '" + eventType + "' was NOT received", Status.FAILED);
        }

        return received;
    }

    /**
     * Get all events of a specific type
     */
    public List<BitrepositoryEvent> getEvents(String eventType) {
        return events.stream()
                .filter(e -> e.getType().equals(eventType))
                .collect(Collectors.toList());
    }

    /**
     * Get all captured events
     */
    public List<BitrepositoryEvent> getAllEvents() {
        return List.copyOf(events);
    }

    /**
     * Clear all captured events
     */
    public void clear() {
        allureStep("Clear event log for " + componentName);
        events.clear();
    }

    private BitrepositoryEvent findEvent(String eventType) {
        return events.stream()
                .filter(e -> e.getType().equals(eventType))
                .findFirst()
                .orElse(null);
    }

    /**
     * Attach event summary to Allure report
     */
    public void attachEventSummary() {
        allureStep("Generate event summary");

        if (events.isEmpty()) {
            allureAttachment("Event Summary", "text/plain", "No events captured");
            return;
        }

        StringBuilder summary = new StringBuilder();
        summary.append(String.format(Locale.ROOT, "Total events: %d\n\n", events.size()));

        for (int i = 0; i < events.size(); i++) {
            BitrepositoryEvent event = events.get(i);
            summary.append(String.format(Locale.ROOT, "%d. [%s] %s\n",
                    i + 1,
                    event.getTimestamp(),
                    event.getType()));
        }

        allureAttachment("Event Summary", "text/plain", summary.toString());
    }
}
