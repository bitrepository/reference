package org.bitrepository.pillar.integration.perf;

import java.util.LinkedList;
import java.util.List;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.pillar.integration.PillarIntegrationTest;
import org.bitrepository.pillar.integration.perf.metrics.ConsoleMetricAppender;
import org.bitrepository.pillar.integration.perf.metrics.MetricAppender;
import org.bitrepository.pillar.integration.perf.metrics.Metrics;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class PillarPerformanceTest extends PillarIntegrationTest {
    protected List<MetricAppender> metricAppenders = new LinkedList<MetricAppender>();
    protected CollectionTestHelper collectionHelper;
    protected String[] existingFiles;

    @BeforeSuite
    @Override
    public void initializeSuite() {
        super.initializeSuite();
        defineMetricAppenders();
        initializeCollectionHelper();
    }

    @AfterSuite
    @Override
    public void shutdownSuite() {
        collectionHelper.shutdown();
        super.shutdownSuite();
    }

    private void defineMetricAppenders() {
        metricAppenders.add(new ConsoleMetricAppender());
    }

    private void initializeCollectionHelper() {
        collectionHelper = new CollectionTestHelper(componentSettings, httpServer);
    }

    /**
     * Will block until <code>numberOfOperations</code> file event have been marked in the <code>metrics</code> object.
      */
    protected void awaitAsynchronousCompletion(Metrics metrics, int numberOfOperations) {
        while(metrics.getCount() < numberOfOperations) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("...waiting for the last " + (numberOfOperations - metrics.getCount()) + " files");
        }
    }

    protected class EventHandlerForMetrics implements EventHandler {
        private final Metrics metrics;
        public EventHandlerForMetrics(Metrics metrics) {
            this.metrics = metrics;
        }

        @Override
        public void handleEvent(OperationEvent event) {
            if (event.getType().equals(OperationEvent.OperationEventType.COMPLETE)) {
                // Todo The current complete event should return a event, so we can detect which file has been affected
                this.metrics.mark("#" + metrics.getCount());
            } else if (event.getType().equals(OperationEvent.OperationEventType.FAILED)) {
                this.metrics.registerError(event.getInfo());
            }
        }
    }
}
