package com.underground.extractor.handler;

import java.io.File;

public interface SevenZipHandler {
    boolean extractAll(File file, String password, String outputDir) throws WrongPassException, Exception;
}
