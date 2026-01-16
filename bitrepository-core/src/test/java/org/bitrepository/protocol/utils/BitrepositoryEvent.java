package org.bitrepository.protocol.utils;

import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

public class BitrepositoryEvent {
    private final String type;
    private final Instant timestamp;
    private final Map<String, Object> data;

    public BitrepositoryEvent(String type, Map<String, Object> data) {
        this.type = type;
        this.timestamp = Instant.now();
        this.data = new HashMap<>(data);
    }

    public String getType() {
        return type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getData() {
        return new HashMap<>(data);
    }

    public Object getData(String key) {
        return data.get(key);
    }
}
