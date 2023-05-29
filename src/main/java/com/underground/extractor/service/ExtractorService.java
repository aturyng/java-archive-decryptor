package com.underground.extractor.service;

import com.underground.extractor.handler.RarHandler;
import com.underground.extractor.handler.SevenZipHandler;
import com.underground.extractor.handler.WrongPassException;
import com.underground.extractor.handler.ZipHandler;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExtractorService {

    private final ZipHandler zipHandler;
    private final SevenZipHandler sevenZipHandler;
    private final RarHandler rarHandler;
    Logger logger = LoggerFactory.getLogger(ExtractorService.class);

    public ExtractorService(ZipHandler zipHandler, SevenZipHandler sevenZipHandler, RarHandler rarHandler) {
        this.zipHandler = zipHandler;
        this.sevenZipHandler = sevenZipHandler;
        this.rarHandler = rarHandler;
    }


    public void extract(String inputDir, String outputDir, String passwordsFile, boolean removeAfterExtraction) {

        //read all password from the text file - line by line
        final List<String> allPasswords = new ArrayList<>();
        try {
            allPasswords.addAll(Files.readAllLines(Paths.get(passwordsFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            //go over each archived file
            Files.walkFileTree(Paths.get(inputDir), new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path currPath, BasicFileAttributes attrs) {
                    if (!Files.isDirectory(currPath)) {
                        String fileName = currPath.getFileName().toString();
                        String extension = this.getExtension(fileName);
                        File currFile =  currPath.toFile();
                        int passwordsTried = 0;
                        //try each password
                        passwordsLoop:
                        for (String password : allPasswords) {
                            boolean extractionOK;
                            try {
                                switch (extension) {
                                    case "7zip":
                                    case "7z":
                                        extractionOK = sevenZipHandler.extractArchive(currFile, password, outputDir);
                                        break;
                                    case "7z.001":
                                        extractionOK = sevenZipHandler.extractMultipartArchive(currFile, password, outputDir);
                                        break;
                                    case "zip":
                                        extractionOK = zipHandler.extractArchive(currFile, password, outputDir);
                                        break;
                                    case "rar":
                                        extractionOK = rarHandler.extractArchive(currPath.toFile(), password, outputDir);
                                        break;
                                    default:
                                        logger.warn("Unsupported file format: {}. Ignoring...", fileName);
                                        break passwordsLoop;
                                }
                                if (extractionOK) {
                                    logger.info("Successfully finished extracting {}", fileName);
                                    if (removeAfterExtraction) {
                                        if (currPath.toFile().delete()) {
                                            logger.info("Successfully removed {}", fileName);
                                        } else {
                                            logger.warn("Could not remove {}", fileName);
                                        }
                                        break;
                                    }
                                }

                            }
                            catch (WrongPassException e){
                                passwordsTried++;
                                if (passwordsTried >= allPasswords.size()){
                                    logger.warn("No suitable password for {}", fileName);
                                }
                            }
                            catch (Exception e){
                                logger.warn("Unforeseen exception: {}", e.getMessage());
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                private String getExtension(String fileName) {
                    var extention = FilenameUtils.getExtension(fileName);
                    if (extention.equals("001")) {
                        extention = FilenameUtils.getExtension(fileName.substring(0, fileName.length() - 4)) + ".001";
                    }
                    return extention;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    
}
