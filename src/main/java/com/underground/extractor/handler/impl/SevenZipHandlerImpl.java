package com.underground.extractor.handler.impl;

import com.underground.extractor.handler.SevenZipHandler;
import com.underground.extractor.handler.WrongPassException;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
public class SevenZipHandlerImpl implements SevenZipHandler {
    @Override
    public boolean extractAll(File file, String password, String outputDir) throws WrongPassException {
        try {
            SevenZFile sevenZFile = new SevenZFile(file, password.toCharArray());

            SevenZArchiveEntry entry;
            while ((entry = sevenZFile.getNextEntry()) != null){
                if (entry.isDirectory()){
                    continue;
                }
                File curfile = new File(outputDir, entry.getName());
                File parent = curfile.getParentFile();
                if (!parent.exists()) {
                    parent.mkdirs();
                }
                FileOutputStream out = new FileOutputStream(curfile);
                byte[] content = new byte[(int) entry.getSize()];
                sevenZFile.read(content, 0, content.length);
                out.write(content);
                out.close();
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            if(e.getMessage().contains("Checksum verification failed")){
                throw new WrongPassException("Wrong password", e);
            }
            e.printStackTrace();
        }
        return false;
    }
}
