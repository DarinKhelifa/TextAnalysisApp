package com.example.textanalysisapp.model;

import java.io.File;
import java.nio.file.Files;

public class TextFile {
    private File file;
    private String content;

    public TextFile(File file) {
        this.file = file;
        loadContent();
    }

    private void loadContent() {
        try {
            content = new String(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            content = "";
        }
    }

    public String getContent() {
        return content;
    }

    public File getFile() {
        return file;
    }
}