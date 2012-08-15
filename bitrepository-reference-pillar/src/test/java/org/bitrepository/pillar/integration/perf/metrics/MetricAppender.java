package org.bitrepository.pillar.integration.perf.metrics;
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
