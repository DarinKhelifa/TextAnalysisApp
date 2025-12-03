package com.example.textanalysisapp.controller;

import com.example.textanalysisapp.utils.ErrorHandler;

import java.io.File;
import java.nio.file.Files;

public class FileController {

    /**
     * Read file content with error handling
     */
    public static String readFileContent(File file) throws Exception {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            throw new Exception("Cannot read file. It might be binary or corrupted: " + e.getMessage());
        }
    }

    /**
     * Validate a file before analysis
     */
    public static boolean validateFile(File file) {
        if (!file.exists()) {
            ErrorHandler.showWarning("File not found", "The file does not exist: " + file.getName());
            return false;
        }

        if (file.length() == 0) {
            ErrorHandler.showWarning("Empty file", "The file is empty: " + file.getName());
            return false;
        }

        // Check file extension
        String name = file.getName().toLowerCase();
        String[] allowedExtensions = {".txt", ".md", ".java", ".xml", ".json", ".csv", ".html", ".htm"};

        boolean isTextFile = false;
        for (String ext : allowedExtensions) {
            if (name.endsWith(ext)) {
                isTextFile = true;
                break;
            }
        }

        if (!isTextFile) {
            ErrorHandler.showWarning("Non-text file",
                    "The file may not be a plain text file. Analysis may not work correctly.");
            // Continue anyway, but warn the user
        }

        return true;
    }
}