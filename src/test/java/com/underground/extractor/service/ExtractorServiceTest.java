package com.underground.extractor.service;

import com.underground.extractor.handler.RarHandler;
import com.underground.extractor.handler.SevenZipHandler;
import com.underground.extractor.handler.WrongPassException;
import com.underground.extractor.handler.ZipHandler;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtractorServiceTest {

    private ExtractorService service;

    private ClassLoader classLoader;

    @Mock
    RarHandler rarHandler;
    @Mock
    ZipHandler zipHandler;
    @Mock
    SevenZipHandler sevenZipHandler;

    @TempDir
    private Path tempInputDir;
    @TempDir
    private Path tempOutputDir;

    @BeforeEach
    void setUp() throws IOException {
        FileUtils.cleanDirectory(tempInputDir.toFile());
        FileUtils.cleanDirectory(tempOutputDir.toFile());
        this.classLoader = getClass().getClassLoader();
        this.service = new ExtractorService(zipHandler, sevenZipHandler, rarHandler);
        Stream.of("file.zip", "file.7z", "file.7z.001", "file.rar", "passwords.txt").forEach(fileName -> {
            Path newFilePath = Paths.get(tempInputDir.resolve(fileName).toUri());
            try {
                Files.createFile(newFilePath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    @AfterEach
    void tearDown() throws IOException {
        FileUtils.cleanDirectory(tempInputDir.toFile());
        FileUtils.cleanDirectory(tempOutputDir.toFile());
    }

    @Test
    void givenArchivesAndCorrectPasswordForEachAndRemoveAfterTrue_whenExtractAll_thenAllExtractedAndArchivesRemoved() throws Exception {
        //GIVEN
        //a folder with multiple files and a file with passwords
        File passwordsFile = new File(classLoader.getResource("test_data/passwords.txt").getFile());

        File fileZip = tempInputDir.resolve("file.zip").toFile();
        File file7z = tempInputDir.resolve("file.7z").toFile();
        File file7zMultipart = tempInputDir.resolve("file.7z.001").toFile();
        File fileRar = tempInputDir.resolve("file.rar").toFile();

        Assertions.assertTrue(fileZip.exists());
        Assertions.assertTrue(file7z.exists());
        Assertions.assertTrue(file7zMultipart.exists());
        Assertions.assertTrue(fileRar.exists());

        String inputDir = tempInputDir.toAbsolutePath().toString();
        String outputDir = tempOutputDir.toAbsolutePath().toString();
        String passFilePath = passwordsFile.getAbsolutePath();
        when(zipHandler.extractArchive(fileZip,"12345", outputDir)).thenReturn(true);
        when(sevenZipHandler.extractArchive(file7z,"password", outputDir)).thenReturn(true);
        when(sevenZipHandler.extractArchive(file7z,"12345", outputDir)).thenThrow(WrongPassException.class);
        when(rarHandler.extractArchive(fileRar,"12345", outputDir)).thenReturn(true);
        when(sevenZipHandler.extractMultipartArchive(file7zMultipart,"12345", outputDir)).thenReturn(true);

        //WHEN
        //we run the extractor
        this.service.extract(inputDir,
                outputDir,
                passFilePath,
                true
                );


        //THEN
        //all files we have passwords for have been deleted after extraction
        Assertions.assertFalse(fileZip.exists());
        Assertions.assertFalse(file7z.exists());
        Assertions.assertFalse(fileRar.exists());
        Assertions.assertFalse(file7zMultipart.exists());
        //but password file should stay
        Assertions.assertTrue(passwordsFile.exists());
    }
}