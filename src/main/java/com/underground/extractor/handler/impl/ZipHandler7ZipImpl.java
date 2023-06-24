package com.underground.extractor.handler.impl;

import com.underground.extractor.handler.IArchiveExtractor;
import com.underground.extractor.handler.WrongPassException;
import jakarta.annotation.PostConstruct;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.impl.VolumedArchiveInStream;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

@Component
public class ZipHandler7ZipImpl implements IArchiveExtractor {

    public static final String EXCEPTION_MSG_WRONG_PASS = "Custom: WRONG_PASSWORD";
    public File outputDirectoryFile;
    Logger logger = LoggerFactory.getLogger(ZipHandler7ZipImpl.class);
    private String archive;
    private String outputDirectory;

    @PostConstruct
    public void afterInit() {
        try {
            SevenZip.initSevenZipFromPlatformJAR();
            logger.info("7-Zip-JBinding library was initialized");
        } catch (SevenZipNativeInitializationException e) {
            e.printStackTrace();
        }
    }

    private void prepareOutputDirectory() {
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

    private void doExtract(String password, IInStream streamProcessor, IArchiveOpenCallback callback) throws ExtractionException, WrongPassException {
        IInArchive inArchive;
        try {
            if (callback != null) {
                inArchive = SevenZip.openInArchive(null, streamProcessor, callback);
            } else {
                inArchive = SevenZip.openInArchive(null, streamProcessor, password);
            }
            inArchive.extract(null, false, new ExtractCallback(this, inArchive, password));
        } catch (SevenZipException e) {
            //TODO check for error when opening archive
            if (e.getMessage().contains("Archive file can't be opened with any of the registered codecs")
                    || e.getCause().getMessage().equals(ZipHandler7ZipImpl.EXCEPTION_MSG_WRONG_PASS)) {
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
    public boolean extractArchive(File archiveFile, String archivePassword, String outputDir) throws WrongPassException, ExtractionException {
        this.archive = archiveFile.getAbsolutePath();
        this.outputDirectoryFile = new File(outputDir);
        this.outputDirectory = outputDir;
        prepareOutputDirectory();

        try (var file = new RandomAccessFile(archive, "r")) {
            doExtract(archivePassword, new RandomAccessFileInStream(file), null);
        } catch (FileNotFoundException e) {
            throw new ExtractionException("File not found", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean extractMultipartArchive(File archiveFile, String archivePassword, String outputDir) throws WrongPassException, ExtractionException {
        this.archive = archiveFile.getAbsolutePath();
        this.outputDirectoryFile = new File(outputDir);
        this.outputDirectory = outputDir;
        prepareOutputDirectory();
        try {
            var extension = FilenameUtils.getExtension(archive);
            var callback = new MultipartArchiveOpenCallback();
            if (extension.equals("rar")) {
                IInStream inStream = callback.getStream(archive);
                doExtract(archivePassword, inStream, callback);
            } else {
                doExtract(archivePassword, new VolumedArchiveInStream(archive, callback), null);
            }
        } catch (SevenZipException e) {//TODO
            throw new RuntimeException(e);
        }
        return true;
    }
}
