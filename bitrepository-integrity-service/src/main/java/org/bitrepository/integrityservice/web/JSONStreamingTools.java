package org.bitrepository.integrityservice.web;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JSONStreamingTools {
    private final static Logger log = LoggerFactory.getLogger(JSONStreamingTools.class);
    private final static String JSON_LIST_START = "[";
    private final static String JSON_LIST_END = "]";
    private final static String JSON_LIST_SEPERATOR = ",";
    private final static String JSON_DELIMITER = "\"";
    
    
    /**
     * Helper method to stream integrity issues as JSON for webservices.
     * @param iterator The IntegrityIssueIterator with integrity issues that is to be streamed out.   
     */
    public static StreamingOutput StreamIntegrityIssues(IntegrityIssueIterator iterator) {
        final IntegrityIssueIterator it = iterator;
        return new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    boolean firstIssueWritten = false;
                    String issue;
                    output.write(JSON_LIST_START.getBytes());
                    while((issue = it.getNextIntegrityIssue()) != null) {
                        if(firstIssueWritten) {
                            output.write(JSON_LIST_SEPERATOR.getBytes());
                        }
                        String issueStr = JSON_DELIMITER + issue + JSON_DELIMITER;
                        output.write(issueStr.getBytes());
                        firstIssueWritten = true;
                    }
                    output.write(JSON_LIST_END.getBytes());
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                } finally {
                    try {
                        if(it != null) {
                            it.close();
                        }
                    } catch (Exception e) {
                        log.error("Caught execption when closing IntegrityIssueIterator", e);
                        throw new WebApplicationException(e);
                    }
                }
            }
        };
    }
}
