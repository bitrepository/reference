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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.pillar.integration.CollectionTestHelper;
import org.bitrepository.pillar.integration.PillarIntegrationTest;
import org.bitrepository.pillar.integration.perf.metrics.ConsoleMetricAppender;
import org.bitrepository.pillar.integration.perf.metrics.MetricAppender;
import org.bitrepository.pillar.integration.perf.metrics.Metrics;
import org.testng.annotations.BeforeSuite;

public class PillarPerformanceTest extends PillarIntegrationTest {
    protected List<MetricAppender> metricAppenders = new LinkedList<MetricAppender>();
    protected String[] existingFiles;

    @BeforeSuite
    @Override
    public void initializeSuite() {
        super.initializeSuite();
        defineMetricAppenders();
        initializeCollectionHelper();
    }

    private void defineMetricAppenders() {
        MetricAppender consoleAppender = new ConsoleMetricAppender();
        consoleAppender.disableSingleMeasurement(true);
        metricAppenders.add(consoleAppender);
    }

    private void initializeCollectionHelper() {
        collectionHelper = new CollectionTestHelper(settingsForCUT, httpServer);
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
                    "(" + asPrettyTime(metrics.getStartTime()) + ")");
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

    protected String asPrettyTime(long starttime) {
        long millis = System.currentTimeMillis() - starttime;

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        if (days > 0) {
        sb.append(days);
        sb.append("d ");
        }
        if (hours > 0) {
        sb.append(hours);
        sb.append("h ");
        }
        if (minutes > 0 && days < 0 ) {
        sb.append(minutes);
        sb.append("m ");
        }

        if (seconds > 0 && hours < 0 ) {
        sb.append(seconds);
        sb.append("s");
        }

        if (millis > 0 && minutes < 0 ) {
            sb.append(millis);
            sb.append("s");
        }

        return(sb.toString());
    }
}
