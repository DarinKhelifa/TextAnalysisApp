package com.example.textanalysisapp.controller;

import com.example.textanalysisapp.model.TextAnalyzer;
import javafx.concurrent.Task;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import java.io.File;
import java.util.Map;

public class AnalysisManager {

    private TextAnalyzer textAnalyzer;
    private Task<Map<String, Object>> currentTask;

    public AnalysisManager() {
        this.textAnalyzer = new TextAnalyzer();
    }

    public Task<Map<String, Object>> createAnalysisTask(File file) {
        return new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                // Update progress
                updateProgress(10, 100);
                updateMessage("Validating file...");

                // Validate file
                if (!file.exists()) {
                    throw new Exception("File not found: " + file.getName());
                }
                if (file.length() == 0) {
                    throw new Exception("File is empty: " + file.getName());
                }

                updateProgress(20, 100);
                updateMessage("Reading file...");

                // Read file content
                String content = FileController.readFileContent(file);
                if (content.trim().isEmpty()) {
                    throw new Exception("File contains no text.");
                }

                updateProgress(40, 100);
                updateMessage("Analyzing text...");

                // Analyze text
                Map<String, Object> results = textAnalyzer.analyzeText(content);

                updateProgress(80, 100);
                updateMessage("Preparing results...");

                // Add file information
                results.put("fileName", file.getName());
                results.put("fileSize", file.length() / 1024 + " KB");
                results.put("filePath", file.getAbsolutePath());
                results.put("fileContent", content);

                updateProgress(100, 100);
                updateMessage("Analysis complete!");

                return results;
            }
        };
    }

    public void setupTaskHandlers(Task<Map<String, Object>> task,
                                  Button startBtn,
                                  Button cancelBtn,
                                  ProgressBar progressBar,
                                  Label statusLabel) {

        // Save current task
        currentTask = task;

        // Bind progress bar
        progressBar.progressProperty().bind(task.progressProperty());

        // Bind status messages
        task.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            javafx.application.Platform.runLater(() -> {
                statusLabel.setText(newMsg);
            });
        });

        // On success
        task.setOnSucceeded(event -> {
            javafx.application.Platform.runLater(() -> {
                resetUI(startBtn, cancelBtn, progressBar);
                statusLabel.setText("Analysis complete!");
            });
        });

        // On failure
        task.setOnFailed(event -> {
            javafx.application.Platform.runLater(() -> {
                resetUI(startBtn, cancelBtn, progressBar);
                statusLabel.setText("Analysis failed");
            });
        });

        // On cancellation
        task.setOnCancelled(event -> {
            javafx.application.Platform.runLater(() -> {
                resetUI(startBtn, cancelBtn, progressBar);
                statusLabel.setText("Analysis cancelled");
            });
        });

        // Cancel button
        cancelBtn.setOnAction(e -> {
            if (task.isRunning()) {
                task.cancel();
                javafx.application.Platform.runLater(() -> {
                    statusLabel.setText("Cancelling...");
                });
            }
        });
    }

    private void resetUI(Button startBtn, Button cancelBtn, ProgressBar progressBar) {
        javafx.application.Platform.runLater(() -> {
            startBtn.setDisable(false);
            cancelBtn.setDisable(true);
            progressBar.setVisible(false);
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
        });
    }

    public void cancelCurrentTask() {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
        }
    }
}