package com.example.textanalysisapp.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ErrorHandler {

    public static void showError(String title, Throwable e) {
        Alert alert = new Alert(AlertType.ERROR);// error window
        alert.setTitle("Error");
        alert.setHeaderText(title);
        alert.setContentText(e.getMessage());
        alert.showAndWait();
    }

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}