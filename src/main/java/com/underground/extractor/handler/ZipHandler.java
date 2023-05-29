package com.underground.extractor.handler;

import java.io.File;

public interface ZipHandler {
    boolean extractArchive(File file, String password, String outputDir) throws WrongPassException, Exception;
    boolean extractMultipartArchive(File file, String password, String outputDir) throws WrongPassException, Exception;
}
