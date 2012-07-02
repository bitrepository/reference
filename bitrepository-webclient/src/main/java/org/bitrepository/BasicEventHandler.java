/*
 * #%L
 * Bitrepository Webclient
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
package org.bitrepository;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;

import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic event handler, does nothing more than write the textual description of the event to the event log. 
 */
public class BasicEventHandler implements EventHandler {
    private String logFile;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private ArrayBlockingQueue<String> shortLog;

    public BasicEventHandler(String logFile, ArrayBlockingQueue<String> shortLog) {
        this.shortLog = shortLog;
        this.logFile = logFile;
        String timestamp = DateFormat.getDateTimeInstance().format(new Date());
        BufferedWriter out;
        try {
            String logEntry = "[" + timestamp + "] Webclient started..\n";
            out = new BufferedWriter(new FileWriter(logFile, false));
            out.write(logEntry);
            out.flush();
            this.shortLog.offer(logEntry);
            log.debug("---- Wrote to log ----");
        } catch (IOException e) {
        }
    }

    @Override
    public synchronized void handleEvent(OperationEvent event) {
        BufferedWriter out;
        String timestamp = DateFormat.getDateTimeInstance().format(new Date());
        try {
            String logEntry = "[" + timestamp + "] " + event.toString() + "\n";
            out = new BufferedWriter(new FileWriter(logFile, true));
            out.write(logEntry);
            out.flush();
            shortLog.offer(logEntry);
            if(shortLog.size() > 25) {
                try {
                    shortLog.take();
                } catch (InterruptedException e) {
                }
            }
        } catch (IOException e) {
        }
    }
}
