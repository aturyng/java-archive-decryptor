package com.underground.extractor.handler;

import java.io.File;

public interface ZipHandler {
    boolean extractAll(File file, String password, String outputDir) throws WrongPassException, Exception;
}
