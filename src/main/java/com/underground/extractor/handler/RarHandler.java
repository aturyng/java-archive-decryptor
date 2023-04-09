package com.underground.extractor.handler;

import java.io.File;

public interface RarHandler {
    boolean extractAll(File file, String password, String outputDir) throws Exception;
}
