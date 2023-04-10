package com.underground.extractor.handler.impl;

import com.underground.extractor.handler.RarHandler;
import com.underground.extractor.handler.SevenZipHandler;
import com.underground.extractor.handler.WrongPassException;
import com.underground.extractor.handler.ZipHandler;
import jakarta.annotation.PostConstruct;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class ZipHandler7ZipImpl implements ZipHandler, SevenZipHandler, RarHandler {

    Logger logger = LoggerFactory.getLogger(ZipHandler7ZipImpl.class);
    public static final String EXCEPTION_MSG_WRONG_PASS = "Custom: WRONG_PASSWORD";

    @PostConstruct
    public void afterInit(){
        try {
            SevenZip.initSevenZipFromPlatformJAR();
            logger.info("7-Zip-JBinding library was initialized");
        } catch (SevenZipNativeInitializationException e) {
            e.printStackTrace();
        }
    }

    static class ExtractionException extends Exception {
        @Serial
        private static final long serialVersionUID = -5108931481040742838L;

        ExtractionException(String msg) {
            super(msg);
        }

        public ExtractionException(String msg, Exception e) {
            super(msg, e);
        }
    }

    class ExtractCallback implements ICryptoGetTextPassword, IArchiveExtractCallback {
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

        ExtractCallback(IInArchive inArchive, String password) {
            this.inArchive = inArchive;
            this.password = password;
        }

        @Override
        public void setTotal(long total) throws SevenZipException {

        }

        @Override
        public void setCompleted(long completeValue) throws SevenZipException {

        }

        @Override
        public ISequentialOutStream getStream(int index,
                                              ExtractAskMode extractAskMode) throws SevenZipException {
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
            file = new File(outputDirectoryFile, path);
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
        public void prepareOperation(ExtractAskMode extractAskMode)
                throws SevenZipException {

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
                        logger.info("Extracted " + path);
                        break;
                    case TEST:
                        logger.info("Tested " + path);

                    default:
                }
            }
        }

    }

    private String archive;
    private String outputDirectory;
    private File outputDirectoryFile;
    private boolean test = false;


    private void prepareOutputDirectory() throws ExtractionException {
        outputDirectoryFile = new File(outputDirectory);
        if (!outputDirectoryFile.exists()) {
            if (outputDirectoryFile.mkdirs()) {
                logger.info("Created output directory");
            }
        } else {
            if (outputDirectoryFile.list().length != 0) {
                logger.warn("Output directory not empty: ");
            }
        }
    }


    public void extractArchive(String password) throws ExtractionException, WrongPassException {

        try (var randomAccessFile = new RandomAccessFile(archive, "r")) {
            extractArchive(randomAccessFile, password);
        }  catch (FileNotFoundException e) {
            throw new ExtractionException("File not found", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void extractArchive(RandomAccessFile file, String password) throws ExtractionException, WrongPassException {
        try (var inArchive = SevenZip.openInArchive(null, new RandomAccessFileInStream(file), password)){
            inArchive.extract(null, test, new ExtractCallback(inArchive, password));
        } catch (SevenZipException e) {
            //TODO check for error when opening archive
            if (e.getMessage().contains("Archive file can't be opened with any of the registered codecs")
            || e.getCause().getMessage().equals(ZipHandler7ZipImpl.EXCEPTION_MSG_WRONG_PASS) ) {
                throw new WrongPassException();
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Error extracting archive '");
            stringBuilder.append(archive);
            stringBuilder.append("': ");
            stringBuilder.append(e.getMessage());
            if (e.getCause() != null) {
                stringBuilder.append(" (");
                stringBuilder.append(e.getCause().getMessage());
                stringBuilder.append(')');
            }
            String message = stringBuilder.toString();

            throw new ExtractionException(message, e);
        }
    }



    @Override
    public boolean extractAll(File archiveFile, String archivePassword, String outputDir) throws WrongPassException, ExtractionException {
        this.archive = archiveFile.getAbsolutePath();
        this.outputDirectoryFile = new File(outputDir);
        this.outputDirectory = outputDir;
        prepareOutputDirectory();
        extractArchive(archivePassword);
        return true;
    }
}
