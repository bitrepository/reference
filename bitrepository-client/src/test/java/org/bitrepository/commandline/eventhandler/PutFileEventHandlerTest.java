/*
 * #%L
 * Bitrepository Client
 * %%
 * Copyright (C) 2010 - 2026 The Royal Danish Library and The State Archives, Denmark
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
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.modify.putfile.conversation.PutFileCompletePillarEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PutFileEventHandlerTest {
    private static final String COLLECTION_ID = "collection";

    private final Settings settings = TestSettingsProvider.getSettings(getClass().getSimpleName());
    private final RecordingOutputHandler output = new RecordingOutputHandler();

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void printsLineForPillarWithoutChecksumWhenSaltedChecksumIsRequested() throws DecoderException {
        PutFileEventHandler handler = new PutFileEventHandler(settings, output, hmacSha1Spec());

        handler.handleComponentComplete(new PutFileCompletePillarEvent("checksum-pillar", COLLECTION_ID, null,
                responseInfo(ResponseCode.OPERATION_COMPLETED), null));

        assertEquals(List.of("checksum-pillar \t HMAC_SHA1 \t - \t(no checksum returned, e.g. a checksum pillar)"),
                output.resultLines);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void printsReturnedSaltedChecksum() throws DecoderException {
        PutFileEventHandler handler = new PutFileEventHandler(settings, output, hmacSha1Spec());
        ChecksumDataForFileTYPE checksum = new ChecksumDataForFileTYPE();
        checksum.setChecksumValue(Base16Utils.encodeBase16("cae4d2161fb551e70a4176c1ae463ef37ade11f3"));

        handler.handleComponentComplete(new PutFileCompletePillarEvent("pillar", COLLECTION_ID, checksum,
                responseInfo(ResponseCode.OPERATION_COMPLETED), null));

        assertEquals(List.of("pillar \t HMAC_SHA1 \t cae4d2161fb551e70a4176c1ae463ef37ade11f3 \t*"),
                output.resultLines);
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

    private static class RecordingOutputHandler implements OutputHandler {
        private final List<String> resultLines = new ArrayList<>();

        @Override
        public void resultLine(String line) {
            resultLines.add(line);
        }

        @Override
        public void debug(String debug) {}

        @Override
        public void startupInfo(String info) {}

        @Override
        public void completeEvent(String msg, OperationEvent event) {}

        @Override
        public void warn(String warning) {}

        @Override
        public void error(String error) {}

        @Override
        public void error(String error, Throwable e) {}

        @Override
        public void resultHeader(String header) {}

        @Override
        public void setVerbosity(boolean verbose) {}
    }
}
