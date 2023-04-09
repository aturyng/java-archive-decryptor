package com.underground.extractor;

import com.underground.extractor.service.ExtractorService;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class Commands {

    private final ExtractorService extractorService;


    public Commands(ExtractorService extractorService) {
        this.extractorService = extractorService;
    }

    @ShellMethod()
    public void extract(@ShellOption(value = "inDir") String inputDir,
                        @ShellOption(value = "outDir") String outputDir,
                        @ShellOption(value = "pwFile") String passwordsFile,
                        @ShellOption(value = "rem") boolean removeAfterExtraction) {
        extractorService.extract(inputDir, outputDir, passwordsFile, removeAfterExtraction);
    }
    
}
