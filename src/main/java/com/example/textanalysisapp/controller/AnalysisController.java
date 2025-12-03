package com.example.textanalysisapp.controller;

import com.example.textanalysisapp.view.FileInfo;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class AnalysisController {

    private ProgressBar progressBar;
    private Label totalWordsLabel;
    private Label uniqueWordsLabel;
    private TextArea frequentWordsArea;

    public AnalysisController(ProgressBar progressBar, Label totalWordsLabel, Label uniqueWordsLabel, TextArea frequentWordsArea) {
        this.progressBar = progressBar;
        this.totalWordsLabel = totalWordsLabel;
        this.uniqueWordsLabel = uniqueWordsLabel;
        this.frequentWordsArea = frequentWordsArea;
    }

    public void startAnalysis(FileInfo fileInfo) {
        if (fileInfo == null) return;

        File file = new File(fileInfo.getPath());

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String text = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                String[] words = text.split("\\s+");
                int totalWords = words.length;

                Map<String, Integer> freqMap = new HashMap<>();
                for (int i = 0; i < words.length; i++) {
                    if (isCancelled()) break;
                    String w = words[i].toLowerCase();
                    freqMap.put(w, freqMap.getOrDefault(w, 0) + 1);
                    updateProgress(i + 1, words.length); // ✅ ProgressBar update
                }

                int uniqueWords = freqMap.size();

                List<Map.Entry<String, Integer>> sorted = new ArrayList<>(freqMap.entrySet());
                sorted.sort((a, b) -> b.getValue() - a.getValue());

                StringBuilder frequentWords = new StringBuilder();
                for (int i = 0; i < Math.min(10, sorted.size()); i++) {
                    frequentWords.append(sorted.get(i).getKey())
                            .append(" : ")
                            .append(sorted.get(i).getValue())
                            .append("\n");
                }

                // تحديث Labels و TextArea على JavaFX thread
                Platform.runLater(() -> {
                    totalWordsLabel.setText("Total Words: " + totalWords);
                    uniqueWordsLabel.setText("Unique Words: " + uniqueWords);
                    frequentWordsArea.setText(frequentWords.toString());
                });

                return null;
            }
        };

        progressBar.progressProperty().unbind(); // فك الربط القديم إذا كان
        progressBar.progressProperty().bind(task.progressProperty());
        progressBar.setVisible(true);

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
