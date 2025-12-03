package com.example.textanalysisapp.controller;

import com.example.textanalysisapp.model.*;
import com.example.textanalysisapp.utils.ErrorHandler;
import javafx.concurrent.Task;
import javafx.scene.control.ProgressBar;

public class AnalysisController {
    private TextAnalyzer analyzer = new TextAnalyzer();

    public Task<AnalysisResult> createAnalysisTask(TextFile textFile, ProgressBar progressBar) {
        return new Task<AnalysisResult>() {
            @Override
            protected AnalysisResult call() throws Exception {
                // Simulate progress
                for (int i = 0; i <= 100; i += 10) {
                    updateProgress(i, 100);
                    Thread.sleep(50); // Simulate processing time
                }

                // Perform actual analysis
                AnalysisResult result = analyzer.analyze(textFile);

                updateProgress(100, 100);
                return result;
            }

            @Override
            protected void failed() {
                super.failed();
                ErrorHandler.showError("Analysis failed", getException());
            }
        };
    }
}