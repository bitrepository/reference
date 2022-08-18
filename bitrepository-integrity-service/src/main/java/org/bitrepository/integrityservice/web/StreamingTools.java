/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.web;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Class to handle streaming of different kinds of JSON data
 */
public class StreamingTools {
    private final static Logger log = LoggerFactory.getLogger(StreamingTools.class);

    /**
     * Helper method to stream integrity issues as JSON for webservices.
     *
     * @param iterator The IntegrityIssueIterator with integrity issues that is to be streamed out.
     */
    public static StreamingOutput StreamIntegrityIssues(IntegrityIssueIterator iterator) {
        final IntegrityIssueIterator it = iterator;
        return output -> {
            JsonFactory jf = new JsonFactory();
            JsonGenerator jg = jf.createGenerator(output, JsonEncoding.UTF8);
            try {
                jg.writeStartArray();
                String issue;
                while ((issue = it.getNextIntegrityIssue()) != null) {
                    jg.writeString(issue);
                }
                jg.writeEndArray();
                jg.flush();
                jg.close();
            } catch (Exception e) {
                throw new WebApplicationException(e);
            } finally {
                try {
                    if (it != null) {
                        it.close();
                    }
                } catch (Exception e) {
                    log.error("Caught exception when closing IntegrityIssueIterator", e);
                    throw new WebApplicationException(e);
                }
            }
        };
    }

    /**
     * Helper method to stream whole or parts of a files content as a JSON formatted list
     * (one list entry per line)
     *
     * @param source   The source file
     * @param offset   The number of lines to skip
     * @param maxLines The maximum number of lines to output
     */
    public static StreamingOutput StreamFileParts(File source, int offset, int maxLines) {
        final File input = source;
        return output -> {
            JsonFactory jf = new JsonFactory();
            JsonGenerator jg = jf.createGenerator(output, JsonEncoding.UTF8);

            try (BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(input), StandardCharsets.UTF_8))) {
                int linesRead = 0;
                jg.writeStartArray();
                String line;
                while ((line = b.readLine()) != null) {
                    if (linesRead++ < offset) {
                        continue;
                    }
                    jg.writeString(line);
                    if (linesRead - offset >= maxLines) {
                        break;
                    }
                }
                jg.writeEndArray();
                jg.flush();
                jg.close();
            } catch (Exception e) {
                throw new WebApplicationException(e);
            }
        };
    }

    /**
     * Iterates over the elements in an {@link IntegrityIssueIterator} and returns the items as a list.
     *
     * @param iterator The {@link IntegrityIssueIterator}.
     * @return Returns a {@link List <String>}.
     */
    public static List<String> IteratorToList(IntegrityIssueIterator iterator) {
        List<String> output = new ArrayList<>();
        String issue;
        while ((issue = iterator.getNextIntegrityIssue()) != null) {
            output.add(issue);
        }
        iterator.close();

        return output;
    }

    /**
     * Helper method to create a {@link List<String>} of all or parts of a files' content.
     * @param source The source file
     * @param offset The number of lines to skip
     * @param maxLines The number of lines to read
     * @return Returns a {@link List<String>} of the found content.
     */
    public static List<String> FilePartToList(File source, int offset, int maxLines) {
        List<String> output = new ArrayList<>();

        try (BufferedReader b = new BufferedReader(new InputStreamReader(new FileInputStream(source), StandardCharsets.UTF_8))) {
            int linesRead = 0;
            String line;
            while ((line = b.readLine()) != null) {
                if (linesRead++ < offset) {
                    continue;
                }
                output.add(line);
                if (linesRead - offset >= maxLines) {
                    break;
                }
            }
        } catch (Exception e) {
            throw new WebApplicationException(e);
        }

        return output;
    }
}
