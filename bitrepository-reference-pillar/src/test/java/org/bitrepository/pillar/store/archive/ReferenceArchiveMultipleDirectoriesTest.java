/*
 * #%L
 * Bitrepository Reference Pillar
 * %%
 * Copyright (C) 2010 - 2026 Royal Danish Library and The State Archives, Denmark
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
package org.bitrepository.pillar.store.archive;

import org.bitrepository.TestGroups;
import org.bitrepository.pillar.store.filearchive.ArchiveDirectory;
import org.bitrepository.pillar.store.filearchive.ReferenceArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests how a {@link ReferenceArchive} spread over several archive directories finds the directory holding a file.
 */
class ReferenceArchiveMultipleDirectoriesTest {
    private static final String FILE_ID = "test-file";

    @TempDir
    Path baseDir;
    private Path dir1;
    private Path dir2;
    private ReferenceArchive archive;

    @BeforeEach
    void createArchive() {
        dir1 = baseDir.resolve("dir1");
        dir2 = baseDir.resolve("dir2");
        archive = new ReferenceArchive(List.of(dir1.toString(), dir2.toString()));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void hasFileLooksInEveryDirectory() throws IOException {
        assertFalse(archive.hasFile(FILE_ID));

        createFile(dir2, ArchiveDirectory.ARCHIVE_DIR, FILE_ID);

        assertTrue(archive.hasFile(FILE_ID));
        assertFalse(archive.hasFile("other-file"));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getFileReturnsTheFileFromTheDirectoryHoldingIt() throws IOException {
        Path file = createFile(dir2, ArchiveDirectory.ARCHIVE_DIR, FILE_ID);

        File result = archive.getFile(FILE_ID);

        assertTrue(Files.isSameFile(file, result.toPath()));
        assertThrows(IllegalArgumentException.class, () -> archive.getFile("other-file"));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getAllFileIdsCombinesTheFilesOfAllDirectories() throws IOException {
        assertEquals(List.of(), List.copyOf(archive.getAllFileIds()));

        createFile(dir1, ArchiveDirectory.ARCHIVE_DIR, "file-a");
        createFile(dir2, ArchiveDirectory.ARCHIVE_DIR, "file-b");
        createFile(dir2, ArchiveDirectory.ARCHIVE_DIR, "file-c");

        List<String> fileIds = new ArrayList<>(archive.getAllFileIds());
        Collections.sort(fileIds);
        assertEquals(List.of("file-a", "file-b", "file-c"), fileIds);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void deleteFileRetainsTheFileFromTheDirectoryHoldingIt() throws IOException {
        Path file = createFile(dir2, ArchiveDirectory.ARCHIVE_DIR, FILE_ID);

        archive.deleteFile(FILE_ID);

        assertFalse(Files.exists(file));
        assertTrue(Files.isRegularFile(dir2.resolve(ArchiveDirectory.RETAIN_DIR).resolve(FILE_ID)));
        assertFalse(archive.hasFile(FILE_ID));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void deleteUnknownFileFails() {
        assertThrows(IllegalStateException.class, () -> archive.deleteFile(FILE_ID));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void moveToArchiveUsesTheDirectoryHoldingTheTmpFile() throws IOException {
        createFile(dir2, ArchiveDirectory.TEMPORARY_DIR, FILE_ID);

        archive.moveToArchive(FILE_ID);

        assertTrue(Files.isRegularFile(dir2.resolve(ArchiveDirectory.ARCHIVE_DIR).resolve(FILE_ID)));
        assertFalse(Files.exists(dir2.resolve(ArchiveDirectory.TEMPORARY_DIR).resolve(FILE_ID)));
        assertFalse(Files.exists(dir1.resolve(ArchiveDirectory.ARCHIVE_DIR).resolve(FILE_ID)));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void moveToArchiveWithoutTmpFileFails() {
        assertThrows(IllegalStateException.class, () -> archive.moveToArchive(FILE_ID));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getFileInTmpDirReturnsTheFileFromTheDirectoryHoldingIt() throws IOException {
        Path tmpFile = createFile(dir2, ArchiveDirectory.TEMPORARY_DIR, FILE_ID);

        assertTrue(Files.isSameFile(tmpFile, archive.getFileInTmpDir(FILE_ID).toPath()));
        assertThrows(IllegalStateException.class, () -> archive.getFileInTmpDir("other-file"));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void replaceFileCanSpanDirectories() throws IOException {
        Path oldFile = createFile(dir1, ArchiveDirectory.ARCHIVE_DIR, FILE_ID);
        createFile(dir2, ArchiveDirectory.TEMPORARY_DIR, FILE_ID);

        archive.replaceFile(FILE_ID);

        assertFalse(Files.exists(oldFile));
        assertTrue(Files.isRegularFile(dir1.resolve(ArchiveDirectory.RETAIN_DIR).resolve(FILE_ID)));
        assertTrue(Files.isRegularFile(dir2.resolve(ArchiveDirectory.ARCHIVE_DIR).resolve(FILE_ID)));
    }

    private Path createFile(Path archiveDir, String subDir, String fileID) throws IOException {
        return Files.writeString(archiveDir.resolve(subDir).resolve(fileID), "content");
    }
}
