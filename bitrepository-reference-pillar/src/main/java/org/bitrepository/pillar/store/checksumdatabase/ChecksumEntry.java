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
 */
public class ChecksumEntry {
    protected final String fileID;
    protected final String checksum;
    protected final Instant calculationDate;

    /**
     * @param fileID          The id of the file.
     * @param checksum        The checksum of the file.
     * @param calculationDate The calculation date for the checksum of the file.
     */
    public ChecksumEntry(String fileID, String checksum, Instant calculationDate) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");

        this.fileID = fileID;
        this.checksum = checksum;
        this.calculationDate = calculationDate;
    }

    /**
     * @param fileID          The id of the file.
     * @param checksum        The checksum of the file.
     * @param calculationDate The calculation date for the checksum of the file.
     * @deprecated Use {@link #ChecksumEntry(String, String, Instant)} instead
     */
    @Deprecated(forRemoval = true)
    public ChecksumEntry(String fileID, String checksum, Date calculationDate) {
        this(fileID, checksum, calculationDate != null ? calculationDate.toInstant() : null);
    }

    /**
     * @return The id of the file.
     */
    public String getFileId() {
        return fileID;
    }

    /**
     * @return The checksum of the file.
     */
    public String getChecksum() {
        return checksum;
    }

    /**
     * @return The calculation date for the checksum of the file.
     * @deprecated Use {@link #getCalculationInstant()} instead
     */
    @Deprecated(forRemoval = true)
    public Date getCalculationDate() {
        return calculationDate != null ? Date.from(calculationDate) : null;
    }

    public Instant getCalculationInstant() {
        return calculationDate;
    }
}
