package org.bitrepository.pillar.integration.perf.metrics;

public interface MetricAppender {
    void createNewSection(String sectionName);

    void appendEndStatistic(String operationName, long time, int numberOfFiles);

    void appendPartStatistic(String operationName, long time, int numberOfFiles, int count);

    /**
     * Used for to log add a measurement for a file operation.
     */
    void appendFileStatistic(String operationName, long time, String fileID, int count);

    /**
     * Used for to log add a measurement for a non-file operation.
     * */
    void appendSingleStatistic(String operationName, long time, String message, int count);

    void logError(String info);

    /** Can be used to disable single measurement logging. This can either to create a smaller log, or leassen the
     * performance impact of the logging.
     */
    void disableSingleMeasurement(boolean shouldDisable);
}
