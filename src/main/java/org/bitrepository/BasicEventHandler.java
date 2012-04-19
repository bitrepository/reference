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
 *	Basic event handler, does nothing more than write the textual description of the event to the event log. 
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
    
    @SuppressWarnings("rawtypes")
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
