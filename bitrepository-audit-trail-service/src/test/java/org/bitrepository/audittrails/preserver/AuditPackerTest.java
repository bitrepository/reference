package org.bitrepository.audittrails.preserver;

import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.settings.referencesettings.AuditTrailPreservation;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class AuditPackerTest extends ExtendedTestCase {
    private String collectionID;
    private AuditTrailPreservation preservationSettings;
    private AuditTrailStore store;

    @BeforeClass(alwaysRun = true)
    public void setup() {
        Settings settings = TestSettingsProvider.reloadSettings("LocalAuditPreservationUnderTest");
        preservationSettings = settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailPreservation();
        collectionID = settings.getCollections().get(0).getID();
        SettingsUtils.initialize(settings);
        store = mock(AuditTrailStore.class);
    }

    @Test
    public void testCreateNewPackage() {
        AuditPacker packer = new AuditPacker(store, preservationSettings, collectionID);
        Map<String, Long> seqNumsReached = packer.getSequenceNumbersReached();
        assertEquals(seqNumsReached.entrySet().size(), 3);
        assertEquals(packer.getPackedAuditCount(), 0);

        // Create a stubbed event iterator for each expected contributor containing only one event.
        List<StubAuditEventIterator> iterators = List.of(
                new StubAuditEventIterator(), new StubAuditEventIterator(), new StubAuditEventIterator());

        when(store.getAuditTrailsByIterator(
                any(), eq(collectionID), anyString(), any(Long.class), any(), any(), any(), any(), any(), any(), any())
        ).thenReturn(iterators.get(0)).thenReturn(iterators.get(1)).thenReturn(iterators.get(2));

        // Do the actual call to createNewPackage - this will fetch first event from the iterators.
        packer.createNewPackage();
        Long[] expectedSeqNums = {1L, 1L, 1L};
        assertEquals(packer.getPackedAuditCount(), 3);
        assertEquals(packer.getSequenceNumbersReached().values().toArray(new Long[0]), expectedSeqNums);

        // As the iterators have no new audits there should be no newly packed audits on a new call.
        packer.createNewPackage();
        assertEquals(packer.getPackedAuditCount(), 0);
        assertEquals(packer.getSequenceNumbersReached().values().toArray(new Long[0]), expectedSeqNums);
    }
}
