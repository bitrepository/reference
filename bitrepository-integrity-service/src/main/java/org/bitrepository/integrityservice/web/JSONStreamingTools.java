package org.bitrepository.integrityservice.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
    
    /**
     * Helper method to stream whole or parts of a files content as a JSON formatted list
     * (one list entry per line)
     * @param source The source file
     * @param offset The number of lines to skip
     * @param maxlines The maximum number of lines to output 
     */
    public static StreamingOutput StreamFileParts(File source, int offset, int maxlines) {
        final File input = source;
        return new StreamingOutput() {
            public void write(OutputStream output) throws IOException, WebApplicationException {
                JsonFactory jf = new JsonFactory();
                JsonGenerator jg = jf.createGenerator(output, JsonEncoding.UTF8);
                
                try (BufferedReader b = new BufferedReader(new FileReader(input));) {
                    int linesRead = 0;
                    jg.writeStartArray();
                    String line;
                    while((line = b.readLine()) != null) {
                        if(linesRead++ <= offset) {
                            continue;
                        }
                        jg.writeString(line);
                        if(linesRead - offset >= maxlines) {
                            break;
                        }
                    }
                    jg.writeEndArray();
                    jg.flush();
                    jg.close();
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                }
            }
        };
    }
}
