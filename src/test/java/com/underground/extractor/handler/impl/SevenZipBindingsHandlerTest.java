package com.underground.extractor.handler.impl;

import com.underground.extractor.handler.WrongPassException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SevenZipBindingsHandlerTest {
    private ZipHandler7ZipImpl handler;
    private ClassLoader classLoader;
    private String archivePassword;
    private String wrongArchivePassword;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        this.handler = new ZipHandler7ZipImpl();
        this.classLoader = getClass().getClassLoader();
        this.archivePassword = "12345";
        this.wrongArchivePassword = "I am a wrong password";
    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.cleanDirectory(tempDir.toFile());
    }


    private enum FileType {
        SINGLE, MULTIPLE, SINGLE_ENCR_LIST, MULTIPART_SINGLE
    }

    private String getArchiveMask(FileType type, String format) {
        return switch (type) {
            case MULTIPART_SINGLE -> {
                if (format.equals("7z")) {
                    yield "single_file_inside.%s.001";
                } else {
                    yield  "single_file_inside.part01.%s";
                }
            }
            case SINGLE  -> "single_file_inside.%s";
            case MULTIPLE -> "multiple_files_inside.%s";
            case SINGLE_ENCR_LIST -> "encr_file_list-single_file.%s";
        };
    }

    private String getFileInsideMask(FileType type) {
        return switch (type) {
            case MULTIPART_SINGLE -> "%s_multipart_single_file.txt";
            case SINGLE -> "%s_single_file.txt";
            case MULTIPLE -> "%s_file_%d.txt";
            case SINGLE_ENCR_LIST -> "%s_encr_file_list-single_file.txt";
        };
    }


    private String getFileContentMask(FileType type) {
        return switch (type) {
            case MULTIPART_SINGLE -> "%s_CHANGE";
            case SINGLE -> "%s_single_file_content";
            case MULTIPLE -> "%s_file_%d_content";
            case SINGLE_ENCR_LIST -> "%s_encr_file_list-single_file_content";
        };
    }

    private void doTestWithFilesInside(String format, int nrFilesInside, FileType type) throws ExtractionException, WrongPassException, IOException {
        //GIVEN
        File archiveFile = new File(classLoader.getResource(String.format("test_data/" + getArchiveMask(type, format), format)).getFile());

        //WHEN
        boolean extractionResult = false;
        switch (type) {
            case MULTIPART_SINGLE -> {
                extractionResult = this.handler.extractMultipartArchive(archiveFile, archivePassword, tempDir.toAbsolutePath().toString());
            }
            default -> {
                extractionResult = this.handler.extractArchive(archiveFile, archivePassword, tempDir.toAbsolutePath().toString());
            }
        }

        //THEN
        assertTrue(extractionResult, "Should return true");
        for (int i = 1; i <= nrFilesInside; i++) {
            File extractedFile = tempDir.resolve(String.format(getFileInsideMask(type), format, i)).toFile();
            assertTrue(extractedFile.exists(), "Extracted file should exist");
            if (type == FileType.MULTIPART_SINGLE) {
                var referenceFile = new File(classLoader.getResource("test_data/multipart_single_file.txt").getFile());
                assertTrue(areOfSameContent(extractedFile, referenceFile), "Archived long txt file should preserve its content");
            } else {
                List<String> lines = FileUtils.readLines(extractedFile, Charset.defaultCharset());
                assertEquals(1, lines.size(), "Archived txt file should contain only one line");
                assertEquals(String.format(String.format(getFileContentMask(type), format, i)), lines.get(0), "Should contain predefined content");
            }
        }
    }

    private boolean areOfSameContent(File file1, File file2) throws IOException {
        return Files.mismatch(file1.toPath(), file2.toPath()) == -1L;
    }

    private void doTestWithMultipleFilesInside(String format) throws ExtractionException, WrongPassException, IOException {
        this.doTestWithFilesInside(format, 2, FileType.MULTIPLE);
    }


    @Test
    void extractAll_pw_prot_zip_single_file_inside() throws ExtractionException, WrongPassException, IOException {
        this.doTestWithFilesInside("zip", 1, FileType.SINGLE);
    }

    @Test
    void extractAll_pw_prot_7z_single_file_inside() throws ExtractionException, WrongPassException, IOException {
        this.doTestWithFilesInside("7z", 1, FileType.SINGLE);
    }

    @Test
    void extractAll_pw_prot_7z_single_file_inside_multiple_archives() throws ExtractionException, WrongPassException, IOException {
        this.archivePassword = "12345";
        this.doTestWithFilesInside("7z", 1, FileType.MULTIPART_SINGLE);
    }

    @Test
    void extractAll_pw_prot_rar_single_file_inside_multiple_archives() throws ExtractionException, WrongPassException, IOException {
        this.archivePassword = "12345";
        this.doTestWithFilesInside("rar", 1, FileType.MULTIPART_SINGLE);
    }

    @Test
    void extractAll_pw_prot_7z_encr_file_list_single_file_inside() throws ExtractionException, WrongPassException, IOException {
        this.doTestWithFilesInside("7z", 1, FileType.SINGLE_ENCR_LIST);
    }

    @Test
    void extractAll_pw_prot_rar_single_file_inside() throws ExtractionException, WrongPassException, IOException {
        this.doTestWithFilesInside("rar", 1, FileType.SINGLE);
    }

    @Test
    void extractAll_pw_prot_7z_multiple_files_inside() throws ExtractionException, WrongPassException, IOException {
        this.doTestWithMultipleFilesInside("7z");
    }

    @Test
    void extractAll_pw_prot_zip_multiple_files_inside() throws ExtractionException, WrongPassException, IOException {
        this.doTestWithMultipleFilesInside("zip");
    }

    @Test
    void extractAll_pw_prot_rar_multiple_files_inside() throws ExtractionException, WrongPassException, IOException {
        this.doTestWithMultipleFilesInside("rar");
    }

    private void doTestWrongPassException(String format, FileType type) {
        //GIVEN
        File archiveFile = new File(classLoader.getResource(String.format("test_data/" + getArchiveMask(type, format), format)).getFile());

        //WHEN
        assertThrows(WrongPassException.class, () -> {
            boolean extractionResult = this.handler.extractArchive(archiveFile, wrongArchivePassword, tempDir.toAbsolutePath().toString());
            //THEN
            assertFalse(extractionResult);
        }, "WrongPassException was expected");
    }

    @Test()
    void extractAll_pw_prot_zip_single_file_inside_wrong_pass() {
        doTestWrongPassException("zip", FileType.SINGLE);
    }

    @Test()
    void extractAll_pw_prot_7z_single_file_inside_wrong_pass() {
        doTestWrongPassException("7z", FileType.SINGLE);
    }

    @Test()
    void extractAll_pw_prot_7z_encr_file_list_single_file_inside_wrong_pass() {
        doTestWrongPassException("7z", FileType.SINGLE_ENCR_LIST);
    }

    @Test()
    void extractAll_pw_prot_rar_single_file_inside_wrong_pass() {
        doTestWrongPassException("rar", FileType.SINGLE);
    }

    @Test()
    void extractAll_pw_prot_zip_multiple_files_inside_wrong_pass() {
        doTestWrongPassException("zip", FileType.MULTIPLE);
    }

    @Test()
    void extractAll_pw_prot_7z_multiple_files_inside_wrong_pass() {
        doTestWrongPassException("7z", FileType.MULTIPLE);
    }

    @Test()
    void extractAll_pw_prot_rar_multiple_files_inside_wrong_pass() {
        doTestWrongPassException("rar", FileType.MULTIPLE);
    }
}