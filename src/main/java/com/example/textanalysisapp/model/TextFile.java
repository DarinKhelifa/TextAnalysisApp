package com.example.textanalysisapp.model;

import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class TextFile {
    private File file;
    private String content;
    private String encoding;

    public TextFile(File file) {
        this.file = file;
        this.encoding = "UTF-8";
        loadContent();
    }

    private void loadContent() {
        try {
            content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            content = "";
            throw new RuntimeException("Failed to load file: " + e.getMessage());
        }
    }

    public String getContent() { return content; }
    public File getFile() { return file; }
    public String getEncoding() { return encoding; }
}
