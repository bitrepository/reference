package org.bitrepository.pillar.integration.perf;
/*
 * #%L
 * Bitrepository Reference Pillar
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

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.pillar.integration.PillarIntegrationTest;
import org.bitrepository.pillar.integration.perf.metrics.ConsoleMetricAppender;
import org.bitrepository.pillar.integration.perf.metrics.MetricAppender;
import org.bitrepository.pillar.integration.perf.metrics.Metrics;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.annotations.BeforeSuite;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PillarPerformanceTest extends PillarIntegrationTest {
    protected final Logger log = LoggerFactory.getLogger(getClass());
    protected List<MetricAppender> metricAppenders = new LinkedList<>();
    protected String[] existingFiles;

    @BeforeSuite
    @Override
    public void initializeSuite(ITestContext testContext) {
        super.initializeSuite(testContext);
        defineMetricAppenders();
    }

    private void defineMetricAppenders() {
        MetricAppender consoleAppender = new ConsoleMetricAppender();
        consoleAppender.disableSingleMeasurement(true);
        metricAppenders.add(consoleAppender);
    }

    @Override
    protected void addReceiver(MessageReceiver receiver) {
        // Do Nothing, test receivers should be used in performance tests.
    }
    /**
     * Avoid usind messagebuswrapper.
     */
    @Override
    protected void setupMessageBus() {
        super.setupMessageBus();
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settingsForCUT, securityManager);
    }

    /**
     * Will block until <code>numberOfOperations</code> file event have been marked in the <code>metrics</code> object.
     */
    protected void awaitAsynchronousCompletion(Metrics metrics, int numberOfOperations) {
        while(metrics.getCount() < numberOfOperations) {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("...waiting for the last " + (numberOfOperations - metrics.getCount()) +
                    " operations to finish " +
                    "(" + TimeUtils.millisecondsToHuman(metrics.getStartTime()) + ")");
        }
    }

    protected class EventHandlerForMetrics implements EventHandler {
        private final Metrics metrics;
        public EventHandlerForMetrics(Metrics metrics) {
            this.metrics = metrics;
        }

        @Override
        public void handleEvent(OperationEvent event) {
            if (event.getEventType().equals(OperationEvent.OperationEventType.COMPLETE)) {
                // Todo The current complete event should return a event, so we can detect which file has been affected
                this.metrics.mark("#" + metrics.getCount());
            } else if (event.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
                this.metrics.registerError(event.getInfo());
                this.metrics.mark("#" + metrics.getCount());
            }
        }
    }

    protected class ParallelOperationLimiter {
        private final BlockingQueue<String> activeOperationss;

        ParallelOperationLimiter(int limit) {
            activeOperationss = new LinkedBlockingQueue<String>(limit);
        }

        void addJob(String fileID) {
            try {
                activeOperationss.put(fileID);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        void removeJob(String fileID) {
            activeOperationss.remove(fileID);
        }
    }

    protected class OperationEventHandlerForMetrics implements EventHandler {
        private final Metrics metrics;
        private final ParallelOperationLimiter operationLimiter;
        public OperationEventHandlerForMetrics(Metrics metrics, ParallelOperationLimiter putLimiter) {
            this.metrics = metrics;
            this.operationLimiter = putLimiter;
        }

        @Override
        public void handleEvent(OperationEvent event) {
            if (event.getEventType().equals(OperationEvent.OperationEventType.COMPLETE)) {
                log.debug("Received " + event.getOperationType() + " complete event for " + event.getFileID());
                this.metrics.mark("#" + metrics.getCount());
                operationLimiter.removeJob(event.getFileID());
            } else if (event.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
                log.debug("Received " + event.getOperationType() + " failed event for " + event.getFileID());
                this.metrics.registerError(event.getInfo());
                operationLimiter.removeJob(event.getFileID());
            }
        }
    }

    protected class MessageHandlerForMetrics implements MessageListener {
        private final Metrics metrics;
        private final ParallelOperationLimiter operationLimiter;
        public MessageHandlerForMetrics(Metrics metrics, ParallelOperationLimiter putLimiter) {
            this.metrics = metrics;
            this.operationLimiter = putLimiter;
        }

        @Override
        public void onMessage(Message message, MessageContext messageContext) {
            if (message instanceof MessageResponse) {
                MessageResponse response = (MessageResponse)message;
                if (response.getResponseInfo().getResponseCode().equals(ResponseCode.OPERATION_COMPLETED)) {
                    log.debug("Received " + response.getClass().getSimpleName() +
                            " complete message(" + response.getCorrelationID() + ")");
                    this.metrics.mark("#" + metrics.getCount());
                    operationLimiter.removeJob(response.getCorrelationID());
                } else if (response.getResponseInfo().getResponseCode().equals(ResponseCode.FAILURE)) {
                    log.debug("Received " + response.getClass().getSimpleName() +
                            " failure message(" + response.getCorrelationID() + ")");
                    this.metrics.registerError(response.getCorrelationID());
                    operationLimiter.removeJob(response.getCorrelationID());
                }
            }
        }
    }
}
