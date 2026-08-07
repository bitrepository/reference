/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.store.checksumdatabase;

import org.bitrepository.common.ArgumentValidator;

import java.time.Instant;
import java.util.Date;

/**
 * Container for the information about the checksum of a file.
 *
 * @param fileID             The id of the file.
 * @param checksum           The checksum of the file.
 * @param calculationInstant The calculation date for the checksum of the file.
 */
public record ChecksumEntry(String fileID, String checksum, Instant calculationInstant) {

    public ChecksumEntry {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
    }

    /**
     * @deprecated Use {@link #ChecksumEntry(String, String, Instant)} instead
     */
    @Deprecated(forRemoval = true)
    public ChecksumEntry(String fileID, String checksum, Date calculationDate) {
        this(fileID, checksum, calculationDate != null ? calculationDate.toInstant() : null);
    }

    /**
     * @deprecated Use {@link #fileID()} instead
     */
    @Deprecated(forRemoval = true)
    public String getFileId() {
        return fileID;
    }

    /**
     * @deprecated Use {@link #checksum()} instead
     */
    @Deprecated(forRemoval = true)
    public String getChecksum() {
        return checksum;
    }

    /**
     * @deprecated Use {@link #calculationInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public Instant getCalculationInstant() {
        return calculationInstant;
    }

    /**
     * @deprecated Use {@link #getCalculationInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public Date getCalculationDate() {
        return calculationInstant != null ? Date.from(calculationInstant) : null;
    }
}
