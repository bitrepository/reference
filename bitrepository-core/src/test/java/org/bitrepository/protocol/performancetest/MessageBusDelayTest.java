/*
 * #%L
 * Bitrepository Protocol
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.protocol.performancetest;

import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.pillar.integration.ArtemisFixedPortContainer;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.protocol.message.ExampleMessageFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.settings.repositorysettings.MessageBusConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.time.temporal.ChronoUnit.MILLIS;
import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
@Disabled
@Tag(TestGroups.STRESS_TEST)
public class MessageBusDelayTest {
    private Settings settings;
    private SecurityManager securityManager;
    private static final int PERFORMANCE_COUNT = 1000;
    private static final int NUMBER_OF_TESTS = 100;
    private static final boolean WRITE_RESULTS_TO_DISC = true;

    @Container
    static ArtemisContainer activemqContainer = new ArtemisContainer("apache/artemis:2.55.0")
                                                            .withEnv("ANONYMOUS_LOGIN","true");

    @BeforeAll
    public void setup() {
        settings = TestSettingsProvider.reloadSettings(getClass().getSimpleName());
        securityManager = new DummySecurityManager();
        settings.getRepositorySettings().getProtocolSettings().getMessageBusConfiguration()
                .setURL(activemqContainer.getBrokerUrl());
    }

    @Test
    @Tag(TestGroups.STRESS_TEST)
    public void testManyTimes() {
        for (int i = 0; i < NUMBER_OF_TESTS; i++) {
            try {
                performStatisticalAnalysisOfMessageDelay();
                System.out.println("Test " + i + " done.");
            } catch (Exception e) {
                System.err.println("Unknown exception caught: " + e);
            }
        }
    }

    public void performStatisticalAnalysisOfMessageDelay() throws Exception {
        addDescription("This test has the purpose of sending a lot of messages and calculating some statistics "
                + "on the delay between the sending and the retrieval of the message.");
        addStep("Setup the variables and connections for the test.", "Should connect to the messagebus.");
        MessageBus messageBus = MessageBusManager.getMessageBus(settings, securityManager);
        String destination = "DelayPerformanceTestDestination-" + Instant.now().toEpochMilli();
        List<Long> delayList;
        try (MessageReceiver destinationReceiver = new MessageReceiver("Performance test topic receiver")) {
            messageBus.addListener(destination, destinationReceiver.getMessageListener());

            delayList = new ArrayList<>(PERFORMANCE_COUNT);
            AlarmMessage message = ExampleMessageFactory.createMessage(AlarmMessage.class);
            message.setDestination(destination);

            addStep("Sending the message and calculating the time.",
                    "Should be done '" + PERFORMANCE_COUNT + "' times.");
            for (int i = 0; i < PERFORMANCE_COUNT; i++) {
                Instant before = Instant.now();
                messageBus.sendMessage(message);
                AlarmMessage received = destinationReceiver.waitForMessage(AlarmMessage.class, 100, TimeUnit.SECONDS,
                                                                           message.getCorrelationID());
                Instant after = Instant.now();
                if (received == null) {
                    System.err.println("No message received within 100 seconds");
                }

                long delay = MILLIS.between(before, after);
                delayList.add(delay);
            }
        }

        addStep("Perform the statistical analysis on the delay results.", "TODO !!!!");
        calculateStatistics(delayList);
    }

    private void calculateStatistics(List<Long> list) throws Exception {
        Collections.sort(list);
        long maximum = list.get(list.size() - 1);
        long minimum = list.get(0);
        long median = calculateMedian(list);
        long average = calculateAverage(list);
        double deviation = calculateStdDeviation(list, average);

        if (WRITE_RESULTS_TO_DISC) {
            try (FileOutputStream fos = new FileOutputStream("statistic-" + Instant.now().toEpochMilli())) {
                fos.write(("Maximum;" + maximum + "\n").getBytes(StandardCharsets.UTF_8));
                fos.write(("Minimum;" + minimum + "\n").getBytes(StandardCharsets.UTF_8));
                fos.write(("Median;" + median + "\n").getBytes(StandardCharsets.UTF_8));
                fos.write(("Average;" + average + "\n").getBytes(StandardCharsets.UTF_8));
                fos.write(("StdDeviation;" + deviation + "\n").getBytes(StandardCharsets.UTF_8));
                fos.write("\n".getBytes(StandardCharsets.UTF_8));

                for (Long l : list) {
                    fos.write((l + "\n").getBytes(StandardCharsets.UTF_8));
                }
            }
        }
        System.out.println("Maximum;" + maximum);
        System.out.println("Minimum;" + minimum);
        System.out.println("Median;" + median);
        System.out.println("Average;" + average);
        System.out.println("StdDeviation;" + deviation);
    }

    private long calculateMedian(List<Long> list) {
        if (list.size() % 2 == 0) {
            return (list.get(list.size() / 2) + list.get(list.size() / 2 - 1)) / 2;
        } else {
            return list.get(list.size() / 2);
        }
    }

    private long calculateAverage(List<Long> list) {
        long total = 0;
        for (Long l : list) {
            total += l;
        }
        return total / list.size();
    }

    private double calculateStdDeviation(List<Long> list, Long average) {
        long deviationSquared = 0;
        for (Long l : list) {
            deviationSquared += (l - average) * (l - average);
        }
        return Math.sqrt(deviationSquared) / (double) list.size();
    }
}
