/*
 * #%L
 * Bitmagasin integrationstest
 *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.common.utils;

import java.time.Duration;
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

    /**
     * <p>Converts this {@code CountAndTimeUnit} to a {@code Duration}.</p>
     *
     * <p>So the opposite conversion of {@link TimeUtils#durationToCountAndTimeUnit(Duration)}
     * except this conversion also handles negative counts.</p>
     *
     * @throws ArithmeticException by overflow of the range of {@code Duration}
     * (roughly +/- 292 277 024 626 years; numerically large counts of minutes, hours and days may overflow).
     */
    public Duration toDuration() {
        return unit.toChronoUnit().getDuration().multipliedBy(count);
    }

}
