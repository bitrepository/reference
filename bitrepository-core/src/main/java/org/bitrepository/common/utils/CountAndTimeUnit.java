package org.bitrepository.common.utils;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CountAndTimeUnit {
    private final long count;
    private final TimeUnit unit;

    public CountAndTimeUnit(long count, TimeUnit unit) {
        this.count = count;
        this.unit = Objects.requireNonNull(unit, "unit");
    }

    public long getCount() {
        return count;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountAndTimeUnit that = (CountAndTimeUnit) o;
        return count == that.count && unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, unit);
    }
}
