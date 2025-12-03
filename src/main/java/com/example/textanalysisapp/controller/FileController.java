package com.example.textanalysisapp.controller;

import com.example.textanalysisapp.model.TextFile;
import com.example.textanalysisapp.utils.ErrorHandler;
import javafx.scene.control.Alert;
import java.io.File;

public class FileController {

    public static TextFile loadTextFile(File file) {
        try {
            if (!file.exists()) {
                throw new IllegalArgumentException("File does not exist: " + file.getPath());
            }

            if (file.length() == 0) {
                throw new IllegalArgumentException("File is empty: " + file.getName());
            }

            // Check if it's a text file (basic check)
            String name = file.getName().toLowerCase();
            if (!name.endsWith(".txt") && !name.endsWith(".md") && !name.endsWith(".java")
                    && !name.endsWith(".xml") && !name.endsWith(".json")) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Non-Text File Warning");
                alert.setHeaderText("File may not be plain text");
                alert.setContentText("The selected file may not be a plain text file. Analysis may not work correctly.");
                alert.showAndWait();
            }

            return new TextFile(file);

        } catch (Exception e) {
            ErrorHandler.showError("Failed to load file", e);
            return null;
        }
    }
}