package org.bitrepository.integrityservice.web;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Class to handle streaming of different kinds of JSON data 
 */
public class JSONStreamingTools {
    private final static Logger log = LoggerFactory.getLogger(JSONStreamingTools.class);
    
    /**
     * Helper method to stream integrity issues as JSON for webservices.
     * @param iterator The IntegrityIssueIterator with integrity issues that is to be streamed out.   
     */
    public static StreamingOutput StreamIntegrityIssues(IntegrityIssueIterator iterator) {
        final IntegrityIssueIterator it = iterator;
        return new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {
                JsonFactory jf = new JsonFactory();
                JsonGenerator jg = jf.createGenerator(output, JsonEncoding.UTF8);
                try {
                    jg.writeStartArray();
                    String issue;
                    while((issue = it.getNextIntegrityIssue()) != null) {
                        jg.writeString(issue);
                    }
                    jg.writeEndArray();
                    jg.flush();
                    jg.close();
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
