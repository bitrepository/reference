/*
 * #%L
 * Bitrepository Client
 * %%
 * Copyright (C) 2010 - 2025 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.commandline.eventhandler;

import org.apache.commons.codec.DecoderException;
import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.modify.putfile.conversation.PutFileCompletePillarEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PutFileEventHandlerTest {
    private static final String COLLECTION_ID = "collection";

    private final Settings settings = TestSettingsProvider.getSettings(getClass().getSimpleName());
    private final OutputHandler output = mock(OutputHandler.class);

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void printsDefaultChecksumWithNoteWhenFileAlreadyExisted() throws DecoderException {
        PutFileEventHandler handler = new PutFileEventHandler(settings, output, hmacSha1Spec());
        ChecksumDataForFileTYPE checksum = new ChecksumDataForFileTYPE();
        checksum.setChecksumValue(Base16Utils.encodeBase16("d41d8cd98f00b204e9800998ecf8427e"));

        handler.handleComponentComplete(new PutFileCompletePillarEvent("pillar", COLLECTION_ID, checksum,
                responseInfo(ResponseCode.DUPLICATE_FILE_FAILURE), null));

        verify(output).resultLine("pillar \t " + ChecksumUtils.getDefaultChecksumType(settings).value()
                + " \t d41d8cd98f00b204e9800998ecf8427e \t(file already existed, HMAC_SHA1 not calculated)");
    }

    private static ChecksumSpecTYPE hmacSha1Spec() throws DecoderException {
        ChecksumSpecTYPE spec = new ChecksumSpecTYPE();
        spec.setChecksumType(ChecksumType.HMAC_SHA1);
        spec.setChecksumSalt(Base16Utils.encodeBase16("abab"));
        return spec;
    }

    private static ResponseInfo responseInfo(ResponseCode responseCode) {
        ResponseInfo info = new ResponseInfo();
        info.setResponseCode(responseCode);
        return info;
    }
}
