package com.underground.extractor;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void rarMultipartFilesNamesMatchedCorrectly() {
        //GIVEN
        String filename1 = "something.part1.rar";
        String filename2 = "something.part09.rar";
        String filename3 = "dsdf5434-ffsdV.df.part199.rar";

        //WHEN
        boolean matched1 = Utils.isMultipartArchive(filename1);
        boolean matched2 = Utils.isMultipartArchive(filename2);
        boolean matched3 = Utils.isMultipartArchive(filename3);


        //THEN
        String errorMessage = "Filename '%s' was not matched by regex as RAR multipart archive, but should've been!";
        assertTrue(matched1, String.format(errorMessage, filename1));
        assertTrue(matched2, String.format(errorMessage, filename2));
        assertTrue(matched3, String.format(errorMessage, filename3));

    }

    @Test
    void svenZipMultipartFilesNamesMatchedCorrectly() {
        //GIVEN
        String filename1 = "something.7z.001";
        String filename2 = "something.7z.1";
        String filename3 = "something.7z.985";
        String filename4 = "dsfs45.-34tred.7z.001";
        String filename5 = "dsfs45.-34tred.7zip.001";

        //WHEN
        boolean matched1 = Utils.isMultipartArchive(filename1);
        boolean matched2 = Utils.isMultipartArchive(filename2);
        boolean matched3 = Utils.isMultipartArchive(filename3);
        boolean matched4 = Utils.isMultipartArchive(filename4);
        boolean matched5 = Utils.isMultipartArchive(filename5);


        //THEN
        String errorMessage = "Filename '%s' was not matched by regex as 7z multipart archive, but should've been!";
        assertTrue(matched1, String.format(errorMessage, filename1));
        assertTrue(matched2, String.format(errorMessage, filename2));
        assertTrue(matched3, String.format(errorMessage, filename3));
        assertTrue(matched4, String.format(errorMessage, filename4));
        assertTrue(matched5, String.format(errorMessage, filename5));

    }

    @Test
    void svenZipFilesNamesShouldNotMatchAsMultipart() {
        //GIVEN
        String filename1 = "something.7z";
        String filename2 = "something007.7z";
        String filename3 = "dsfs45.-34tred.7z";

        //WHEN
        boolean matched1 = Utils.isMultipartArchive(filename1);
        boolean matched2 = Utils.isMultipartArchive(filename2);
        boolean matched3 = Utils.isMultipartArchive(filename3);


        //THEN
        String errorMessage = "Filename '%s' was matched by regex as 7z multipart archive, but should NOT have been!";
        assertFalse(matched1, String.format(errorMessage, filename1));
        assertFalse(matched2, String.format(errorMessage, filename2));
        assertFalse(matched3, String.format(errorMessage, filename3));

    }

    @Test
    void rarFirstMultipartFilesMatchedCorrectly() {
        //GIVEN
        String filename1 = "something.part001.rar";
        String filename2 = "something005.part1.rar";
        String filename3 = "something01.part10.rar";
        String filename4 = "so1mething.part002.rar";

        //WHEN
        boolean matched1 = Utils.isFirstMultipartArchive(filename1);
        boolean matched2 = Utils.isFirstMultipartArchive(filename2);
        boolean matched3 = Utils.isFirstMultipartArchive(filename3);
        boolean matched4 = Utils.isFirstMultipartArchive(filename4);


        //THEN
        String errorMessage = "Filename '%s' was not matched by regex as first rar multipart archive, but should've been!";
        assertTrue(matched1, String.format(errorMessage, filename1));
        assertTrue(matched2, String.format(errorMessage, filename2));
        //shoudl not match
        errorMessage = "Filename '%s' was matched by regex as first rar multipart archive, but should NOT have been!";
        assertFalse(matched3, String.format(errorMessage, filename3));
        assertFalse(matched4, String.format(errorMessage, filename4));

    }


    @Test
    void sevenZipFirstMultipartFilesMatchedCorrectly() {
        //GIVEN
        String filename1 = "some-thingrsfsdvf4.7z.001";
        String filename2 = "something05.7z.1";
        String filename3 = "something.7z.010";
        String filename4 = "something.7z.002";

        //WHEN
        boolean matched1 = Utils.isFirstMultipartArchive(filename1);
        boolean matched2 = Utils.isFirstMultipartArchive(filename2);
        boolean matched3 = Utils.isFirstMultipartArchive(filename3);
        boolean matched4 = Utils.isFirstMultipartArchive(filename4);


        //THEN
        String errorMessage = "Filename '%s' was not matched by regex as first 7z multipart archive, but should've been!";
        assertTrue(matched1, String.format(errorMessage, filename1));
        assertTrue(matched2, String.format(errorMessage, filename2));
        //shoudl not match
        errorMessage = "Filename '%s' was matched by regex as first 7z multipart archive, but should NOT have been!";
        assertFalse(matched3, String.format(errorMessage, filename3));
        assertFalse(matched4, String.format(errorMessage, filename4));

    }

    @Test
    void rarFilesNamesShouldNotMatchAsMultipart() {
        //GIVEN
        String filename1 = "something.rar";
        String filename2 = "something007.008.rar";
        String filename3 = "dsfs45.-34tred.rar";

        //WHEN
        boolean matched1 = Utils.isMultipartArchive(filename1);
        boolean matched2 = Utils.isMultipartArchive(filename2);
        boolean matched3 = Utils.isMultipartArchive(filename3);


        //THEN
        String errorMessage = "Filename '%s' was matched by regex as rar multipart archive, but should NOT have been!";
        assertFalse(matched1, String.format(errorMessage, filename1));
        assertFalse(matched2, String.format(errorMessage, filename2));
        assertFalse(matched3, String.format(errorMessage, filename3));

    }


    @Test()
    void fileNameShouldMatchSupportedArchiveFormatsByExtension() {
        //GIVEN
        String filename1 = "something999-..rar";
        String filename2 = "something007.7z";
        String filename3 = "dsfs45.-34tred.7zip";
        String filename4 = "hallo.zip";


        //WHEN
        boolean matched1 = Utils.isSinglepartArchive(filename1);
        boolean matched2 = Utils.isSinglepartArchive(filename2);
        boolean matched3 = Utils.isSinglepartArchive(filename3);
        boolean matched4 = Utils.isSinglepartArchive(filename4);


        //THEN
        String errorMessage = "Filename '%s' was NOT matched by regex as one of supported singlepart archive, but should have been!";
        assertTrue(matched1, String.format(errorMessage, filename1));
        assertTrue(matched2, String.format(errorMessage, filename2));
        assertTrue(matched3, String.format(errorMessage, filename3));
        assertTrue(matched4, String.format(errorMessage, filename4));
    }

    @Test()
    void nonSupportedFileExtensionsAreNotMatchedWithSinglepartMatcher() {
        //GIVEN
        String filename1 = "awesome_movie.avi";
        String filename2 = "awesome_movie.mp4";
        String filename3 = "awesome_movie.mkv";
        String filename4 = "music.mp3";


        //WHEN
        boolean matched1 = Utils.isSinglepartArchive(filename1);
        boolean matched2 = Utils.isSinglepartArchive(filename2);
        boolean matched3 = Utils.isSinglepartArchive(filename3);
        boolean matched4 = Utils.isSinglepartArchive(filename4);


        //THEN
        String errorMessage = "Filename '%s', NOT an archive, was matched by regex as singlepart archive, but should NOT have been!";
        assertFalse(matched1, String.format(errorMessage, filename1));
        assertFalse(matched2, String.format(errorMessage, filename2));
        assertFalse(matched3, String.format(errorMessage, filename3));
        assertFalse(matched4, String.format(errorMessage, filename4));
    }

    @Test()
    void nonSupportedFileExtensionsAreNotMatchedWithMultipartMatcher() {
        //GIVEN
        String filename1 = "awesome_movie.avi";
        String filename2 = "awesome_movie.mp4";
        String filename3 = "awesome_movie.mkv";
        String filename4 = "music.mp3";


        //WHEN
        boolean matched1 = Utils.isMultipartArchive(filename1);
        boolean matched2 = Utils.isMultipartArchive(filename2);
        boolean matched3 = Utils.isMultipartArchive(filename3);
        boolean matched4 = Utils.isMultipartArchive(filename4);


        //THEN
        String errorMessage = "Filename '%s', NOT an archive, was matched by regex as multipart archive, but should NOT have been!";
        assertFalse(matched1, String.format(errorMessage, filename1));
        assertFalse(matched2, String.format(errorMessage, filename2));
        assertFalse(matched3, String.format(errorMessage, filename3));
        assertFalse(matched4, String.format(errorMessage, filename4));
    }

    @Test
    void counterOfMultipartFileExtractedCorrectly(){
        //Given
        String filename1 = "file1.part1.rar";
        String filename2 = "something006.part005.rar";
        String filename3 = "file2.7z.001";
        String filename4 = "filename001.7z.2";


        //WHEN
        Utils.Pair<Integer, Integer> indexes1 = Utils.getIndexesForCounterOfMultipartFilename(filename1);
        Utils.Pair<Integer, Integer> indexes2 = Utils.getIndexesForCounterOfMultipartFilename(filename2);
        Utils.Pair<Integer, Integer> indexes3 = Utils.getIndexesForCounterOfMultipartFilename(filename3);
        Utils.Pair<Integer, Integer> indexes4 = Utils.getIndexesForCounterOfMultipartFilename(filename4);



        //THEN
        String errorMessage = "The counter part of '%s' multipart file was not extracted correctly!";
        assertEquals("1", filename1.substring(indexes1.getKey(), indexes1.getValue()) , String.format(errorMessage, filename1));
        assertEquals("005", filename2.substring(indexes2.getKey(), indexes2.getValue()), String.format(errorMessage, filename2));
        assertEquals("001", filename3.substring(indexes3.getKey(), indexes3.getValue()), String.format(errorMessage, filename3));
        assertEquals("2", filename4.substring(indexes4.getKey(), indexes4.getValue()), String.format(errorMessage, filename4));
    }

    @Test
    void counterInFilenameIsIncrementedCorrectly(){
        //Given
        String filename1 = "file1.part1.rar";
        String filename2 = "something006.part005.rar";
        String filename3 = "file2.7z.001";
        String filename4 = "filename001.7z.2";


        //WHEN
        String nextFilename1 = Utils.getNextMultipartByIncrementingCounter(filename1);
        String nextFilename2 = Utils.getNextMultipartByIncrementingCounter(filename2);
        String nextFilename3 = Utils.getNextMultipartByIncrementingCounter(filename3);
        String nextFilename4 = Utils.getNextMultipartByIncrementingCounter(filename4);


        //THEN
        String errorMessage = "The filename '%s' of multipart file was not incremented correctly!";
        assertEquals("file1.part2.rar", nextFilename1 , String.format(errorMessage, filename1));
        assertEquals("something006.part006.rar", nextFilename2 , String.format(errorMessage, filename2));
        assertEquals("file2.7z.002", nextFilename3 , String.format(errorMessage, filename3));
        assertEquals("filename001.7z.3", nextFilename4 , String.format(errorMessage, filename4));

    }
}