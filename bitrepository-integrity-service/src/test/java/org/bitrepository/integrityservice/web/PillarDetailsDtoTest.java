package org.bitrepository.integrityservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.*;

public class PillarDetailsDtoTest {
    private final ObjectMapper objectMapper = new ObjectMapper(); // Use ObjectMapper for JSON

    @Test
    void testSerialization() throws IOException {
        PillarDetailsDto dto = new PillarDetailsDto("id", "pillar", "TYPE", "admin");

        String jsonOutput = objectMapper.writeValueAsString(dto);

        assertTrue(jsonOutput.contains("\"pillarID\":\"id\""));
        assertTrue(jsonOutput.contains("\"pillarName\":\"pillar\""));
        assertTrue(jsonOutput.contains("\"pillarType\":\"TYPE\""));
        assertTrue(jsonOutput.contains("\"pillarDeleteFileApprover\":\"admin\""));
    }
}