package com.underground.extractor.service;

import com.underground.extractor.Utils;
import com.underground.extractor.handler.IArchiveExtractor;
import com.underground.extractor.handler.WrongPassException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class ExtractorService {

    private final IArchiveExtractor archiveExtractor;
    Logger logger = LoggerFactory.getLogger(ExtractorService.class);

    public ExtractorService(IArchiveExtractor archiveExtractor) {
        this.archiveExtractor = archiveExtractor;
    }


    public void extract(String inputDir, String outputDir, String passwordsFile, boolean removeAfterExtraction) {

        //read all password from the text file - line by line
        final List<String> allPasswords = new ArrayList<>();
        try {
            allPasswords.addAll(Files.readAllLines(Paths.get(passwordsFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (removeAfterExtraction) {
            logger.warn("User requested removal of the file(s) after extraction!");
        }

        try {
            //go over each archived file
            Collection<File> filesToRemove = new ArrayList<>();
            Files.walkFileTree(Paths.get(inputDir), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path currPath, BasicFileAttributes attrs) {
                    if (!Files.isDirectory(currPath)) {
                        String fileName = currPath.getFileName().toString();
                        File currFile = currPath.toFile();

                        int passwordsTried = 0;
                        //try each password
                        for (String password : allPasswords) {
                            boolean extractionOK;
                            FileType fileType;

                            try {
                                final String startExtractMsg = "Starting to extract {}";
                                if (Utils.isFirstMultipartArchive(fileName)) {
                                    fileType = FileType.MULTIPART_ARCHIVE;
                                    logger.info(startExtractMsg, fileName);
                                    extractionOK = archiveExtractor.extractMultipartArchive(currFile, password, outputDir);
                                } else if (Utils.isSinglepartArchive(fileName)) {
                                    fileType = FileType.SINGLEPART_ARCHIVE;
                                    logger.info(startExtractMsg, fileName);
                                    extractionOK = archiveExtractor.extractArchive(currFile, password, outputDir);
                                } else {
                                    logger.warn("Unsupported file format or not first multipart archive: {}. Ignoring...", fileName);
                                    break;
                                }

                                if (extractionOK) {
                                    logger.info("Successfully finished extracting {}", fileName);
                                    if (removeAfterExtraction) {
                                        if (fileType.equals(FileType.SINGLEPART_ARCHIVE)) {
                                            filesToRemove.add(currFile);
                                            break;
                                        } else {
                                            Collection<File> parts = getAllParts(currFile, inputDir);
                                            filesToRemove.addAll(parts);
                                        }
                                    }
                                } else {
                                    logger.warn("Could not finish extracting {}", fileName);
                                }

                            } catch (WrongPassException e) {
                                passwordsTried++;
                                if (passwordsTried >= allPasswords.size()) {
                                    logger.warn("No suitable password for {}", fileName);
                                }
                            } catch (Exception e) {
                                logger.warn("Unforeseen exception: {}", e.getMessage());
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }


            });
            //remove files
            filesToRemove.forEach(file -> {
                if (file.delete()) {
                    logger.info("Successfully removed {}", file.getName());
                } else {
                    logger.warn("Could not remove {}", file.getName());
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private Collection<File> getAllParts(File currFile, String inputDir) {

        String fileName = currFile.getName();
        Collection<File> files = new ArrayList<>();
        //add first
        files.add(currFile);

        //iterate over the rest
        String incrementedFileName = Utils.getNextMultipartByIncrementingCounter(fileName);
        File nextFile = Paths.get(inputDir, incrementedFileName).toFile();
        while (nextFile.exists()) {
            files.add(nextFile);
            incrementedFileName = Utils.getNextMultipartByIncrementingCounter(incrementedFileName);
            nextFile = Paths.get(inputDir, incrementedFileName).toFile();
        }
        return files;
    }

    private enum FileType {
        SINGLEPART_ARCHIVE, MULTIPART_ARCHIVE
    }

}
