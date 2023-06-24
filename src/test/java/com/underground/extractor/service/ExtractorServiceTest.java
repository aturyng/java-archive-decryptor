package com.underground.extractor.service;

import com.underground.extractor.handler.IArchiveExtractor;
import com.underground.extractor.handler.WrongPassException;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtractorServiceTest {

    @Mock
    IArchiveExtractor archiveExtractor;
    private ExtractorService service;
    private ClassLoader classLoader;
    @TempDir
    private Path tempInputDir;
    @TempDir
    private Path tempOutputDir;

    @BeforeEach
    void setUp() throws IOException {
        FileUtils.cleanDirectory(tempInputDir.toFile());
        FileUtils.cleanDirectory(tempOutputDir.toFile());
        this.classLoader = getClass().getClassLoader();
        this.service = new ExtractorService(archiveExtractor);
        Stream.of("file.zip", "file.7z", "file.7z.001", "file.7z.002", "file.7z.003", "file.rar", "passwords.txt").forEach(fileName -> {
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
        when(archiveExtractor.extractArchive(fileZip, "12345", outputDir)).thenReturn(true);
        when(archiveExtractor.extractArchive(file7z, "password", outputDir)).thenReturn(true);
        when(archiveExtractor.extractArchive(file7z, "12345", outputDir)).thenThrow(WrongPassException.class);
        when(archiveExtractor.extractArchive(fileRar, "12345", outputDir)).thenReturn(true);
        when(archiveExtractor.extractMultipartArchive(file7zMultipart, "12345", outputDir)).thenReturn(true);

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

    @Test
    void allPartsOfSevenZipMultipartArchiveRemoveWhenRemovalSetToTrue() throws Exception {
        //GIVEN
        //a folder with multiple files and a file with passwords
        File passwordsFile = new File(classLoader.getResource("test_data/passwords.txt").getFile());

        File fileZip = tempInputDir.resolve("file.zip").toFile();
        File file7z = tempInputDir.resolve("file.7z").toFile();
        File file7zMultipart1 = tempInputDir.resolve("file.7z.001").toFile();
        File file7zMultipart2 = tempInputDir.resolve("file.7z.002").toFile();
        File file7zMultipart3 = tempInputDir.resolve("file.7z.003").toFile();
        File fileRar = tempInputDir.resolve("file.rar").toFile();

        Assertions.assertTrue(fileZip.exists());
        Assertions.assertTrue(file7z.exists());
        Assertions.assertTrue(file7zMultipart1.exists());
        Assertions.assertTrue(file7zMultipart2.exists());
        Assertions.assertTrue(file7zMultipart3.exists());
        Assertions.assertTrue(fileRar.exists());

        String inputDir = tempInputDir.toAbsolutePath().toString();
        String outputDir = tempOutputDir.toAbsolutePath().toString();
        String passFilePath = passwordsFile.getAbsolutePath();

        //nothing is extracted except for multipart
        when(archiveExtractor.extractArchive(eq(fileZip), anyString(), eq(outputDir))).thenReturn(false);
        when(archiveExtractor.extractArchive(eq(file7z), anyString(), eq(outputDir))).thenReturn(false);
        when(archiveExtractor.extractArchive(eq(fileRar), anyString(), eq(outputDir))).thenReturn(false);
        when(archiveExtractor.extractMultipartArchive(eq(file7zMultipart1), eq("password"), eq(outputDir))).thenReturn(false);
        //only this gets extracted
        when(archiveExtractor.extractMultipartArchive(file7zMultipart1, "12345", outputDir)).thenReturn(true);

        //WHEN
        //we run the extractor
        this.service.extract(inputDir,
                outputDir,
                passFilePath,
                true
        );

        //THEN
        //Should NOT be removed
        Assertions.assertTrue(fileZip.exists());
        Assertions.assertTrue(file7z.exists());
        Assertions.assertTrue(fileRar.exists());
        Assertions.assertTrue(passwordsFile.exists());
        //Should be removed -- most importantly, ALL parts of multipart archive, not only the first part
        final String errorMsg = "Multipart file '%s' should have been deleted!";
        Assertions.assertFalse(file7zMultipart1.exists(), String.format(errorMsg, file7zMultipart1));
        Assertions.assertFalse(file7zMultipart2.exists(), String.format(errorMsg, file7zMultipart2));
        Assertions.assertFalse(file7zMultipart3.exists(), String.format(errorMsg, file7zMultipart3));

    }
}