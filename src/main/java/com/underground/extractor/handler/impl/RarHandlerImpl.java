package com.underground.extractor.handler.impl;

import com.github.junrar.Junrar;
import com.underground.extractor.handler.RarHandler;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class RarHandlerImpl implements RarHandler {
    @Override
    public boolean extractAll(File file, String password, String outputDir) throws Exception {

        Junrar.extract(file.getAbsolutePath(), outputDir, password);
        //TODO handle exception properly
        return true;

    }
}
