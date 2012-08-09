package org.bitrepository.pillar.integration.perf.metrics;

public class ConsoleMetricAppender implements MetricAppender {
    private boolean disableSingleMeasurement = false;

    @Override
    public void createNewSection(String sectionName){
        System.out.println("\nStarting " + sectionName);
    }

    @Override
    public void appendEndStatistic(String operationName, long time, int numberOfFiles) {
        System.out.println("\tTook " + time + "ms for the last " + numberOfFiles + " " + operationName + "s." +
                "\tAverage time was " + time / numberOfFiles + "ms.\n");
    }

    @Override
    public void appendPartStatistic(String operationName, long time, int numberOfFiles, int count) {
        System.out.println("\tTook " + time + "ms for the last " + numberOfFiles + " " + operationName + "s." +
                "\tAverage time was " + time / numberOfFiles + "ms");
    }

    @Override
    public void appendFileStatistic(String operationName, long time, String fileID, int count) {
        if (!disableSingleMeasurement) {
            System.out.println(time + "ms\tto " + operationName + " " + fileID);
        }
    }

    @Override
    public void appendSingleStatistic(String operationName, long time, String message, int count) {
        if (!disableSingleMeasurement) {
            System.out.println(time + "ms\tto " + operationName + "#" + count + ", " + message);
        }
    }

    @Override
    public void logError(String info) {
        System.out.println(info);
    }

    @Override
    public void disableSingleMeasurement(boolean shouldDisable) {
        this.disableSingleMeasurement = shouldDisable;
    }
}
