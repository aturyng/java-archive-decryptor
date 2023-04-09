package com.underground.extractor.handler.impl;

import com.underground.extractor.handler.WrongPassException;
import com.underground.extractor.handler.ZipHandler;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class ZipHandlerImpl implements ZipHandler {
    @Override
    public boolean extractAll(File file, String password, String outputDir) throws WrongPassException {

        try (ZipFile zipFile = new ZipFile(file, password.toCharArray())){
            zipFile.extractAll(outputDir);
            return true;
        } catch (ZipException e) {
            if (e.getType() == ZipException.Type.WRONG_PASSWORD) {
                throw new WrongPassException("Wrong password", e);
            } else {
                //Corrupt file
                e.printStackTrace();
            }
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
