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

import org.apache.commons.codec.DecoderException;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Container of the results of a checksum database extraction.
 */
public class ExtractedChecksumResultSet {
    protected final List<ChecksumDataForChecksumSpecTYPE> entries;
    protected boolean moreEntriesReported;
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    public ExtractedChecksumResultSet() {
        entries = new ArrayList<>();
        moreEntriesReported = false;
    }

    /**
     * Adds an entry to this result set.
     *
     * @param entry The entry to add.
     */
    public void insertChecksumEntry(ChecksumDataForChecksumSpecTYPE entry) {
        entries.add(entry);
    }

    /**
     * Adds an entry to this result set.
     *
     * @param entry The entry to add.
     */
    public void insertChecksumEntry(ChecksumEntry entry) {
        ChecksumDataForChecksumSpecTYPE res = new ChecksumDataForChecksumSpecTYPE();
        res.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(entry.getCalculationDate()));
        try {
            res.setChecksumValue(Base16Utils.encodeBase16(entry.getChecksum()));
        } catch (DecoderException e) {
            throw new IllegalArgumentException("Could not encode checksum.", e);
        }

        res.setFileID(entry.getFileId());
        entries.add(res);
    }

    /**
     * @return A list with all the reported entries.
     */
    public List<ChecksumDataForChecksumSpecTYPE> getEntries() {
        return new ArrayList<>(entries);
    }

    /**
     * Set that more entries has been found.
     */
    public void reportMoreEntriesFound() {
        moreEntriesReported = true;
    }

    /**
     * @return Whether it has been reported, that more results exists.
     */
    public boolean hasMoreEntries() {
        return moreEntriesReported;
    }
}
