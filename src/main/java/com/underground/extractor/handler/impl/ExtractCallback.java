package com.underground.extractor.handler.impl;

import net.sf.sevenzipjbinding.*;

import java.io.*;

class ExtractCallback implements ICryptoGetTextPassword, IArchiveExtractCallback {
    private final ZipHandler7ZipImpl zipHandler7Zip;
    private final IInArchive inArchive;
    private int index;
    private OutputStream outputStream;
    private File file;
    private ExtractAskMode extractAskMode;
    private boolean isFolder;

    private final String password;

    public String cryptoGetTextPassword() {
        return password;
    }

    ExtractCallback(ZipHandler7ZipImpl zipHandler7Zip, IInArchive inArchive, String password) {
        this.zipHandler7Zip = zipHandler7Zip;
        this.inArchive = inArchive;
        this.password = password;
    }

    @Override
    public void setTotal(long total) {

    }

    @Override
    public void setCompleted(long completeValue) {

    }

    @Override
    public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
        closeOutputStream();

        this.index = index;
        this.extractAskMode = extractAskMode;
        this.isFolder = (Boolean) inArchive.getProperty(index,
                PropID.IS_FOLDER);

        if (extractAskMode != ExtractAskMode.EXTRACT) {
            // Skipped files or files being tested
            return null;
        }

        String path = (String) inArchive.getProperty(index, PropID.PATH);
        file = new File(zipHandler7Zip.outputDirectoryFile, path);
        if (isFolder) {
            createDirectory(file);
            return null;
        }

        createDirectory(file.getParentFile());

        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new SevenZipException("Error opening file: "
                    + file.getAbsolutePath(), e);
        }

        return data -> {
            try {
                outputStream.write(data);
            } catch (IOException e) {
                throw new SevenZipException("Error writing to file: "
                        + file.getAbsolutePath());
            }
            return data.length; // Return amount of consumed data
        };
    }

    private void createDirectory(File parentFile) throws SevenZipException {
        if (!parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                throw new SevenZipException("Error creating directory: "
                        + parentFile.getAbsolutePath());
            }
        }
    }

    private void closeOutputStream() throws SevenZipException {
        if (outputStream != null) {
            try {
                outputStream.close();
                outputStream = null;
            } catch (IOException e) {
                throw new SevenZipException("Error closing file: "
                        + file.getAbsolutePath());
            }
        }
    }

    @Override
    public void prepareOperation(ExtractAskMode extractAskMode) {

    }

    @Override
    public void setOperationResult(ExtractOperationResult extractOperationResult)
            throws SevenZipException {
        closeOutputStream();
        String path = (String) inArchive.getProperty(index, PropID.PATH);
        boolean is7zDataErr = extractOperationResult == ExtractOperationResult.DATAERROR
                && inArchive.getArchiveFormat().equals(ArchiveFormat.SEVEN_ZIP);
        if (extractOperationResult == ExtractOperationResult.WRONG_PASSWORD || is7zDataErr) {
            throw new SevenZipException(ZipHandler7ZipImpl.EXCEPTION_MSG_WRONG_PASS);
        }
        if (extractOperationResult != ExtractOperationResult.OK) {
            throw new SevenZipException("Invalid file: " + path);
        }

        if (!isFolder) {
            switch (extractAskMode) {
                case EXTRACT:
                    zipHandler7Zip.logger.info("Extracted " + path);
                    break;
                case TEST:
                    zipHandler7Zip.logger.info("Tested " + path);

                default:
            }
        }
    }

}
