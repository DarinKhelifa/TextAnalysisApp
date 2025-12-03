package com.example.textanalysisapp.controller;

import com.example.textanalysisapp.model.AnalysisResult;
import com.example.textanalysisapp.model.TextFile;
import javafx.scene.control.*;
import javafx.concurrent.Task; // ADD THIS IMPORT
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class UIController {

    public static void setupAnalysisButton(Button analyzeBtn, Button cancelBtn,
                                           ProgressBar progressBar, Label statusLabel,
                                           AnalysisController analysisCtrl, TextFile textFile) {

        analyzeBtn.setDisable(true);
        cancelBtn.setDisable(false);
        progressBar.setVisible(true);
        statusLabel.setText("Analyzing...");

        Task<AnalysisResult> task = analysisCtrl.createAnalysisTask(textFile, progressBar);

        // Bind progress bar to task progress
        progressBar.progressProperty().bind(task.progressProperty());

        // On success
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                AnalysisResult result = task.getValue();
                // Update UI with result (you'll need to implement this)
                statusLabel.setText("Analysis complete!");
                analyzeBtn.setDisable(false);
                cancelBtn.setDisable(true);
                progressBar.setVisible(false);
            }
        });

        // Cancel button action
        cancelBtn.setOnAction(e -> {
            if (task.isRunning()) {
                task.cancel();
                statusLabel.setText("Analysis cancelled");
                progressBar.setVisible(false);
                analyzeBtn.setDisable(false);
                cancelBtn.setDisable(true);
            }
        });

        // Start the task in a new thread
        new Thread(task).start();
    }
}