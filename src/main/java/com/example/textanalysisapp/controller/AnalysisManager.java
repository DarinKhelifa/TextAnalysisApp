package com.example.textanalysisapp.controller;

import com.example.textanalysisapp.model.TextAnalyzer;
import com.example.textanalysisapp.utils.ErrorHandler;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import java.io.File;
import java.util.Map;

public class AnalysisManager {

    private TextAnalyzer textAnalyzer;

    public AnalysisManager() {
        this.textAnalyzer = new TextAnalyzer();
    }

    /**
     * Create and configure an analysis task
     */
    public Task<Map<String, Object>> createAnalysisTask(File file,
                                                        Button startBtn,
                                                        Button cancelBtn,
                                                        ProgressBar progressBar,
                                                        Label statusLabel) {

        return new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                // Update UI state
                javafx.application.Platform.runLater(() -> {
                    startBtn.setDisable(true);
                    cancelBtn.setDisable(false);
                    progressBar.setVisible(true);
                    progressBar.setProgress(0);
                    statusLabel.setText("Analyzing: " + file.getName() + "...");
                });

                // Check file
                if (!file.exists()) {
                    throw new Exception("File not found: " + file.getName());
                }
                if (file.length() == 0) {
                    throw new Exception("File is empty: " + file.getName());
                }

                // Read file content with progress
                updateProgress(10, 100);
                updateMessage("Reading file...");
                String content = FileController.readFileContent(file);

                if (content.trim().isEmpty()) {
                    throw new Exception("File contains only whitespace.");
                }

                // Perform analysis with progress updates
                updateProgress(30, 100);
                updateMessage("Analyzing text...");

                Map<String, Object> results = textAnalyzer.analyzeText(content);

                // Add file metadata
                results.put("fileName", file.getName());
                results.put("fileSize", file.length() / 1024 + " KB");
                results.put("filePath", file.getAbsolutePath());

                updateProgress(100, 100);
                updateMessage("Analysis complete!");

                return results;
            }
        };
    }

    /**
     * Setup task event handlers
     */
    public void setupTaskHandlers(Task<Map<String, Object>> task,
                                  Button startBtn,
                                  Button cancelBtn,
                                  ProgressBar progressBar,
                                  Label statusLabel,
                                  AnalysisResultCallback callback) {

        // Bind progress
        progressBar.progressProperty().bind(task.progressProperty());

        // Update status messages
        task.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            statusLabel.setText(newMsg);
        });

        // On success
        task.setOnSucceeded(event -> {
            Map<String, Object> results = task.getValue();
            callback.onAnalysisComplete(results);

            // Reset UI
            resetUI(startBtn, cancelBtn, progressBar);
            statusLabel.setText("Analysis complete!");
        });

        // On failure
        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            ErrorHandler.showError("Analysis Failed", exception);

            // Reset UI
            resetUI(startBtn, cancelBtn, progressBar);
            statusLabel.setText("Analysis failed");
        });

        // Cancel button action
        cancelBtn.setOnAction(e -> {
            if (task.isRunning()) {
                task.cancel();
                resetUI(startBtn, cancelBtn, progressBar);
                statusLabel.setText("Analysis cancelled");
            }
        });
    }

    /**
     * Reset UI to initial state
     */
    private void resetUI(Button startBtn, Button cancelBtn, ProgressBar progressBar) {
        startBtn.setDisable(false);
        cancelBtn.setDisable(true);
        progressBar.setVisible(false);
        progressBar.progressProperty().unbind();
    }

    /**
     * Callback interface for analysis results
     */
    public interface AnalysisResultCallback {
        void onAnalysisComplete(Map<String, Object> results);

        void onAnalysisFailed(String errorMessage);

        void onAnalysisCancelled();
    }
}