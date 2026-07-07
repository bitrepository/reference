package org.bitrepository.common.utils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public record CountAndTimeUnit(long count, TimeUnit unit) {
    public CountAndTimeUnit {
        Objects.requireNonNull(unit, "unit");
    }
}
