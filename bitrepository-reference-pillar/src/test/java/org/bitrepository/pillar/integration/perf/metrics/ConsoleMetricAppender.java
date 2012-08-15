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

public class ConsoleMetricAppender implements MetricAppender {
    private boolean disableSingleMeasurement = false;

    @Override
    public void createNewSection(String sectionName){
        System.out.println("\nStarting " + sectionName);
    }

    @Override
    public void appendEndStatistic(String operationName, long time, int numberOfFiles) {
        System.out.println("\tTook " + time + "ms for all " + numberOfFiles + " " + operationName + "s." +
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
