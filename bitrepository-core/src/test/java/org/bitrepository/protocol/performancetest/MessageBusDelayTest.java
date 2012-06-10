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

import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.client.MessageReceiver;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.protocol.message.ExampleMessageFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.jaccept.TestEventManager;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MessageBusDelayTest extends ExtendedTestCase {
    private Settings settings;
    /** The mocked SecurityManager */
    private SecurityManager securityManager;
    
    protected TestEventManager testEventManager = TestEventManager.getInstance();
    
    private static final int PERFORMANCE_COUNT = 1000;
    
    private static final int NUMBER_OF_TESTS = 100;
    
    private static final boolean WRITE_RESULTS_TO_DISC = true;
//    private static final boolean WRITE_RESULTS_TO_DISC = false;
    
    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings(getClass().getSimpleName());
        securityManager = new DummySecurityManager();
    }
    
    
    @Test( groups = {"StressTest"} )
    public void testManyTimes() throws Exception {
        for(int i = 0; i < NUMBER_OF_TESTS; i++) {
            try {
                performStatisticalAnalysisOfMessageDelay();
            } catch (Exception e) {
                System.err.println("Unknown exception caught: " + e);
            }
        }
    }
    
    public void performStatisticalAnalysisOfMessageDelay() throws Exception {
        addDescription("This test has the purpose of sending a lot of messages and calculating some statistics "
                + "on the delay between the sending and the receival of the message.");
        addStep("Setup the variables and connections for the test.", "Should connect to the messagebus.");
        MessageBus messageBus = MessageBusManager.getMessageBus(settings, securityManager);
        MessageReceiver destinationReceiver;
        String destination = "DelayPerformanceTestDestination-" + new Date().getTime();
        destinationReceiver = new MessageReceiver("Performance test topic receiver", null); //testEventManager);
        messageBus.addListener(destination, destinationReceiver.getMessageListener());
        
        List<Long> delayList = new ArrayList<Long>(PERFORMANCE_COUNT);
        AlarmMessage message = ExampleMessageFactory.createMessage(AlarmMessage.class);
        message.setTo(destination);
        
        addStep("Sending the message and calculating the time.", "Should be done '" + PERFORMANCE_COUNT + "' times.");
        for(int i = 0; i < PERFORMANCE_COUNT; i++) {
            Date before = new Date();
            messageBus.sendMessage(message);
            AlarmMessage received = destinationReceiver.waitForMessage(AlarmMessage.class, 100, TimeUnit.SECONDS);
            Date after = new Date();
            if(received == null) {
                System.err.println("No message received within 100 seconds");
            }
            
            long delay = after.getTime() - before.getTime();
            delayList.add(delay);
        }
        
        addStep("Perform the statistical analysis on the delay results.", "TODO !!!!");
        calculateStatistics(delayList);
    }
    
    private void calculateStatistics(List<Long> list) throws Exception {
        Collections.sort(list);
        long maximum = list.get(list.size()-1);
        long minimum = list.get(0);
        long median = calculateMedian(list);
        long average = calculateAverage(list);
        double deviation = calculateStdDeviation(list, average);
        
        if(WRITE_RESULTS_TO_DISC) {
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(new File("statistic-" + new Date().getTime()));
                fos.write(new String("Maximum;" + maximum + "\n").getBytes());
                fos.write(new String("Minimum;" + minimum + "\n").getBytes());
                fos.write(new String("Median;" + median + "\n").getBytes());
                fos.write(new String("Average;" + average + "\n").getBytes());
                fos.write(new String("StdDeviation;" + deviation + "\n").getBytes());
                fos.write(new String("\n").getBytes());
                
                for(Long l : list) {
                    fos.write(new String(l + "\n").getBytes());
                }
            } finally {
                if(fos != null) {
                    fos.close();
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
        
        if(list.size() % 2 == 0) {
            return (list.get(list.size() / 2) + list.get(list.size() / 2 - 1)) / 2; 
        } else {
            return list.get(list.size() / 2);
        }
    }
    
    private long calculateAverage(List<Long> list) {
        long total = 0;
        for(Long l : list) {
            total += l;
        }
        
        return total / list.size();
    }
    
    private double calculateStdDeviation(List<Long> list, Long average) {
        long deviationSquared = 0;
        for(Long l : list) {
            deviationSquared += (l-average) * (l-average);
        }
        return Math.sqrt(deviationSquared) / (double) list.size();
    }
}
