package org.bitrepository.common.utils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public record CountAndTimeUnit(long count, TimeUnit unit) {
    public CountAndTimeUnit {
        Objects.requireNonNull(unit, "unit");
    }

    /**
     * @deprecated Use {@link #count()} instead
     */
    @Deprecated(forRemoval = true)
    public long getCount() {
        return count;
    }

    /**
     * @deprecated Use {@link #unit()} instead
     */
    @Deprecated(forRemoval = true)
    public TimeUnit getUnit() {
        return unit;
    }
}
