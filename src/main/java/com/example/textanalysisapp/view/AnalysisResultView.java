package com.example.textanalysisapp.view;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.Map;

public class AnalysisResultView extends VBox {

    private GridPane statsGrid;
    private TextArea freqArea;
    private TextArea previewArea;
    private Button closeBtn;
    private Button exportTxtBtn;
    private Button exportCsvBtn;
    private Button copyBtn;

    public AnalysisResultView() {
        createUI();
    }

    private void createUI() {
        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: white; -fx-border-color: #ccc; -fx-border-radius: 8;");

        // Title
        Label title = new Label("📊 Analysis Results");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Close button
        closeBtn = new Button("Close");
        closeBtn.setStyle("-fx-background-color: #999; -fx-text-fill: white; -fx-padding: 5 15;");

        HBox titleRow = new HBox(title, closeBtn);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(title, Priority.ALWAYS);

        // Stats Grid
        statsGrid = new GridPane();
        statsGrid.setHgap(15);
        statsGrid.setVgap(8);
        statsGrid.setPadding(new Insets(10));
        statsGrid.setStyle("-fx-background-color: #f5f5f5; -fx-border-radius: 6;");

        // Frequent Words
        Label freqLabel = new Label("🔤 Most Frequent Words:");
        freqLabel.setStyle("-fx-font-weight: bold;");

        freqArea = new TextArea();
        freqArea.setEditable(false);
        freqArea.setWrapText(true);
        freqArea.setPrefHeight(70);
        freqArea.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");

        // Text Preview
        Label previewLabel = new Label("📝 Text Preview:");
        previewLabel.setStyle("-fx-font-weight: bold;");

        previewArea = new TextArea();
        previewArea.setEditable(false);
        previewArea.setWrapText(true);
        previewArea.setPrefHeight(100);
        previewArea.setStyle("-fx-background-color: white; -fx-border-color: #ddd;");

        // Export buttons
        Label exportLabel = new Label("💾 Export Results:");
        exportLabel.setStyle("-fx-font-weight: bold;");

        exportTxtBtn = new Button("Export as TXT");
        exportTxtBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 6 12;");

        exportCsvBtn = new Button("Export as CSV");
        exportCsvBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 6 12;");

        copyBtn = new Button("Copy to Clipboard");
        copyBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 6 12;");

        HBox buttonsRow = new HBox(10, exportTxtBtn, exportCsvBtn, copyBtn);
        buttonsRow.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(
                titleRow,
                statsGrid,
                freqLabel,
                freqArea,
                previewLabel,
                previewArea,
                exportLabel,
                buttonsRow
        );
    }

    public void displayResults(Map<String, Object> results) {
        statsGrid.getChildren().clear();

        // Add statistics to grid
        addStat("Total Words:", results.get("totalWords").toString(), 0);
        addStat("Unique Words:", results.get("uniqueWords").toString(), 1);
        addStat("Characters (with spaces):", results.get("charsWithSpaces").toString(), 2);
        addStat("Sentence Count:", results.get("sentenceCount").toString(), 3);
        addStat("Reading Time:", results.get("readingTime") + " minutes", 4);

        // Show frequent words
        freqArea.setText(results.get("mostFrequent").toString());

        // Show text preview
        String content = (String) results.get("fileContent");
        if (content != null) {
            String preview = content.length() > 500 ? content.substring(0, 500) + "..." : content;
            previewArea.setText(preview);
        }

        setVisible(true);
    }

    private void addStat(String label, String value, int row) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold;");

        Label val = new Label(value);

        statsGrid.add(lbl, 0, row);
        statsGrid.add(val, 1, row);
    }

    // Getters for buttons
    public Button getCloseButton() { return closeBtn; }
    public Button getExportTxtButton() { return exportTxtBtn; }
    public Button getExportCsvButton() { return exportCsvBtn; }
    public Button getCopyToClipboardButton() { return copyBtn; }
}