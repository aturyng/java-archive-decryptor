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
    void tearDown() {

    }

    @Test
    void extractAll_pw_prot_zip_single_file_inside() throws ZipHandler7ZipImpl.ExtractionException, WrongPassException, IOException {
        //GIVEN
        File archiveFile = new File(classLoader.getResource("test_data/single_file_inside.zip").getFile());

        //WHEN
        boolean extractionResult = this.handler.extractAll(archiveFile, archivePassword, tempDir.toAbsolutePath().toString());

        //THEN
        assertTrue(extractionResult, "Should return true");
        File extractedFile = tempDir.resolve("zip_single_file.txt").toFile();
        assertTrue(extractedFile.exists(), "Extracted file should exist");
        List<String> lines = FileUtils.readLines(extractedFile, Charset.defaultCharset());
        assertEquals(1, lines.size(), "Archived txt file should contain only one line");
        assertEquals("zip_single_file_content", lines.get(0), "Should contain predefined content");

    }

    @Test
    void extractAll_pw_prot_7z_single_file_inside() throws ZipHandler7ZipImpl.ExtractionException, WrongPassException, IOException {
        //GIVEN
        File archiveFile = new File(classLoader.getResource("test_data/single_file_inside.7z").getFile());

        //WHEN
        boolean extractionResult = this.handler.extractAll(archiveFile, archivePassword, tempDir.toAbsolutePath().toString());

        //THEN
        assertTrue(extractionResult, "Should return true");
        File extractedFile = tempDir.resolve("7z_single_file.txt").toFile();
        assertTrue(extractedFile.exists(), "Extracted file should exist");
        List<String> lines = FileUtils.readLines(extractedFile, Charset.defaultCharset());
        assertEquals(1, lines.size(), "Archived txt file should contain only one line");
        assertEquals("7z_single_file_content", lines.get(0), "Should contain predefined content");

    }

    @Test
    void extractAll_pw_prot_7z_encr_file_list_single_file_inside() throws ZipHandler7ZipImpl.ExtractionException, WrongPassException, IOException {
        //GIVEN
        File archiveFile = new File(classLoader.getResource("test_data/encr_file_list-single_file.7z").getFile());

        //WHEN
        boolean extractionResult = this.handler.extractAll(archiveFile, archivePassword, tempDir.toAbsolutePath().toString());

        //THEN
        assertTrue(extractionResult, "Should return true");
        File extractedFile = tempDir.resolve("7z_encr_file_list-single_file.txt").toFile();
        assertTrue(extractedFile.exists(), "Extracted file should exist");
        List<String> lines = FileUtils.readLines(extractedFile, Charset.defaultCharset());
        assertEquals(1, lines.size(), "Archived txt file should contain only one line");
        assertEquals("7z_encr_file_list-single_file_content", lines.get(0), "Should contain predefined content");

    }

    @Test
    void extractAll_pw_prot_rar_single_file_inside() throws ZipHandler7ZipImpl.ExtractionException, WrongPassException, IOException {
        //GIVEN
        File archiveFile = new File(classLoader.getResource("test_data/single_file_inside.rar").getFile());

        //WHEN
        boolean extractionResult = this.handler.extractAll(archiveFile, archivePassword, tempDir.toAbsolutePath().toString());

        //THEN
        assertTrue(extractionResult, "Should return true");
        File extractedFile = tempDir.resolve("rar_single_file.txt").toFile();
        assertTrue(extractedFile.exists(), "Extracted file should exist");
        List<String> lines = FileUtils.readLines(extractedFile, Charset.defaultCharset());
        assertEquals(1, lines.size(), "Archived txt file should contain only one line");
        assertEquals("rar_single_file_content", lines.get(0), "Should contain predefined content");

    }

    @Test()
    void extractAll_pw_prot_zip_single_file_inside_wrong_pass() {
        //GIVEN
        File archiveFile = new File(classLoader.getResource("test_data/single_file_inside.zip").getFile());

        //WHEN
        assertThrows(WrongPassException.class, () -> {
            boolean extractionResult = this.handler.extractAll(archiveFile, wrongArchivePassword, tempDir.toAbsolutePath().toString());
            //THEN
            assertFalse(extractionResult);
        }, "WrongPassException was expected");
    }

    @Test()
    void extractAll_pw_prot_7z_single_file_inside_wrong_pass() {
        //GIVEN
        File archiveFile = new File(classLoader.getResource("test_data/single_file_inside.7z").getFile());

        //WHEN
        assertThrows(WrongPassException.class, () -> {
            boolean extractionResult = this.handler.extractAll(archiveFile, wrongArchivePassword, tempDir.toAbsolutePath().toString());
            //THEN
            assertFalse(extractionResult);
        }, "WrongPassException was expected");
    }

    @Test()
    void extractAll_pw_prot_7z_encr_file_list_single_file_inside_wrong_pass() {
        //GIVEN
        File archiveFile = new File(classLoader.getResource("test_data/encr_file_list-single_file.7z").getFile());

        //WHEN
        assertThrows(WrongPassException.class, () -> {
            boolean extractionResult = this.handler.extractAll(archiveFile, wrongArchivePassword, tempDir.toAbsolutePath().toString());
            //THEN
            assertFalse(extractionResult);
        }, "WrongPassException was expected");
    }
    @Test()
    void extractAll_pw_prot_rar_single_file_inside_wrong_pass() {
        //GIVEN
        File archiveFile = new File(classLoader.getResource("test_data/single_file_inside.rar").getFile());

        //WHEN
        assertThrows(WrongPassException.class, () -> {
            boolean extractionResult = this.handler.extractAll(archiveFile, wrongArchivePassword, tempDir.toAbsolutePath().toString());
            //THEN
            assertFalse(extractionResult);
        }, "WrongPassException was expected");
    }
}