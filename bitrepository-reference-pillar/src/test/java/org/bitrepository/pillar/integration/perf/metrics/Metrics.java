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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class Metrics {
    private final String operationName;
    private final int numberOfFiles;
    private final int partInterval;
    private long startTime = 0;
    private int count = 0;
    private long lastMark;
    private long lastPartTime;
    private int lastPartCount = 0;

    private List<MetricAppender> appenderList = new LinkedList<MetricAppender>();

    public Metrics(String operationName, int numberOfFiles, int partInterval) {
        this.operationName = operationName;
        this.numberOfFiles = numberOfFiles;
        this.partInterval = partInterval;
    }

    public synchronized void addAppender(MetricAppender newAppender) {
        appenderList.add(newAppender);
    }
    public synchronized void addAppenders(Collection<MetricAppender> newAppenders) {
        appenderList.addAll(newAppenders);
    }

    public synchronized void start() {
        startTime = System.currentTimeMillis();
        lastMark = System.currentTimeMillis();
        lastPartTime = System.currentTimeMillis();
        count++;
    }

    public synchronized long getStartTime() {
        return startTime;
    }

    public synchronized void mark(String fileID) {
        registerFileStatistic(fileID);
        registerPartStatistic();
        registerEndStatistic();

        lastMark = System.currentTimeMillis();
        count++;
    }

    public synchronized void mark() {
        registerSingleStatistic("");
        registerPartStatistic();
        registerEndStatistic();

        lastMark = System.currentTimeMillis();
        count++;
    }

    public int getCount() {
        return count;
    }

    private void registerEndStatistic() {
        long time = System.currentTimeMillis() - startTime;
        if (count == numberOfFiles) {
            for (MetricAppender appender: appenderList) {
                appender.appendEndStatistic(operationName, time, numberOfFiles);
            }
        }
    }

    private void registerPartStatistic() {
        long time = System.currentTimeMillis() - lastPartTime;
        int partNumberOfFiles = count - lastPartCount;
        if (count - lastPartCount >= partInterval) {
            for (MetricAppender appender: appenderList) {
                appender.appendPartStatistic(operationName, time, partNumberOfFiles, count);
            }
            lastPartTime = System.currentTimeMillis();
            lastPartCount = count;
        }
    }

    private void registerFileStatistic(String fileID) {
        long time = System.currentTimeMillis() - lastMark;
        for (MetricAppender appender: appenderList) {
            appender.appendFileStatistic(operationName, time, fileID, count);
        }
    }

    private void registerSingleStatistic(String message) {
        long time = System.currentTimeMillis() - lastMark;
        for (MetricAppender appender: appenderList) {
            appender.appendSingleStatistic(operationName, time, message, count);
        }
    }

    public void registerError(String info) {
        for (MetricAppender appender: appenderList) {
            appender.logError(info);
        }
    }
}