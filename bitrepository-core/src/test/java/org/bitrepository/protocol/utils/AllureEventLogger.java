package org.bitrepository.protocol.utils;

import com.google.gson.Gson;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
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
        Allure.step("Initialize event logger for: " + componentName);
    }

    /**
     * Log an event (replaces TestEventHandler's event capturing)
     */
    @Step("Event received: {event.type}")
    public void logEvent(BitrepositoryEvent event) {
        events.add(event);

        // Attach to Allure report
        Allure.addAttachment(
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
    @Step("Waiting for event: {eventType} (timeout: {timeout.seconds}s)")
    public BitrepositoryEvent waitForEvent(String eventType, Duration timeout) {
        Instant deadline = Instant.now().plus(timeout);

        while (Instant.now().isBefore(deadline)) {
            BitrepositoryEvent event = findEvent(eventType);
            if (event != null) {
                Allure.step(
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

        Allure.step(
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
    @Step("Verify event received: {eventType}")
    public boolean receivedEvent(String eventType) {
        boolean received = findEvent(eventType) != null;

        if (received) {
            Allure.step("✓ Event '" + eventType + "' was received", Status.PASSED);
        } else {
            Allure.step("✗ Event '" + eventType + "' was NOT received", Status.FAILED);
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
    @Step("Clear event log for {componentName}")
    public void clear() {
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
    @Step("Generate event summary")
    public void attachEventSummary() {
        if (events.isEmpty()) {
            Allure.addAttachment("Event Summary", "text/plain", "No events captured");
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

        Allure.addAttachment("Event Summary", "text/plain", summary.toString());
    }}
