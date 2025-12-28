package com.example.textanalysisapp.controller;

import java.io.File;
import java.nio.file.Files;

public class FileController {

    /**
     * Read file content
     */
    public static String readFileContent(File file) throws Exception {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (Exception e) {
            throw new Exception("Cannot read file: " + file.getName());
        }
    }

    /**
     * Validate file before analysis
     */
    public static boolean validateFile(File file) {
        // Check if file exists
        if (!file.exists()) {
            showWarning("File not found", "The file does not exist: " + file.getName());
            return false;
        }

        // Check if file is empty
        if (file.length() == 0) {
            showWarning("Empty file", "The file is empty: " + file.getName());
            return false;
        }

        return true;
    }

    /**
     * Show warning message
     */
    private static void showWarning(String title, String message) {
        System.out.println("Warning: " + title + " - " + message);
    }
}