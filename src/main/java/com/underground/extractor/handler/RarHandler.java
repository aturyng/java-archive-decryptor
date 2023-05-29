package com.underground.extractor.handler;

import java.io.File;

public interface RarHandler {
    boolean extractArchive(File file, String password, String outputDir) throws Exception;
    boolean extractMultipartArchive(File file, String password, String outputDir) throws WrongPassException, Exception;
}
