package com.example.textanalysisapp.view;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.geometry.Pos;
import javafx.application.Platform;
import java.util.Map;

public class AnalysisResultView extends VBox {

    private Label titleLabel;
    private GridPane statsGrid;
    private TextArea freqArea;
    private TextArea resultsPreviewArea;
    private BarChart<String, Number> frequencyChart;
    private HBox titleBox;
    private VBox exportButtonsBox;
    private HBox exportButtonsContainer;

    // Export buttons
    private Button exportTxtBtn;
    private Button exportCsvBtn;
    private Button copyToClipboardBtn;
    private Button closeResultsBtn;

    public AnalysisResultView() {
        setupUI();
    }

    private void setupUI() {
        setSpacing(12);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: white; -fx-border-color: #d5c6e0; -fx-border-radius: 12; -fx-border-width: 2;");

        // Title with close button
        titleLabel = new Label("📊 Analysis Results");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #192a51;");

        closeResultsBtn = new Button("✕ Close");
        closeResultsBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 15 6 15; -fx-background-radius: 6;");
        closeResultsBtn.setTooltip(new Tooltip("Close results panel"));

        titleBox = new HBox(titleLabel, closeResultsBtn);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        closeResultsBtn.setAlignment(Pos.CENTER_RIGHT);

        VBox.setMargin(titleBox, new Insets(0, 0, 15, 0));

        // Stats Grid
        statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(10);
        statsGrid.setPadding(new Insets(15));
        statsGrid.setStyle("-fx-background-color: #f9f7fa; -fx-border-radius: 10; -fx-border-color: #e6d6e8; -fx-border-width: 1;");

        // Make grid responsive
        for (int i = 0; i < 2; i++) {// responsivity
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(50);
            col.setHgrow(Priority.ALWAYS);
            statsGrid.getColumnConstraints().add(col);
        }

        VBox.setMargin(statsGrid, new Insets(0, 0, 15, 0));

        // Most Frequent Words Section
        Label freqTitle = new Label("🔤 Most Frequent Words:");
        freqTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #192a51;");

        VBox.setMargin(freqTitle, new Insets(0, 0, 8, 0));

        freqArea = new TextArea();
        freqArea.setEditable(false);
        freqArea.setWrapText(true);
        freqArea.setPrefHeight(80);
        freqArea.setStyle("-fx-control-inner-background: white; -fx-border-color: #d5c6e0; -fx-border-radius: 6; -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");

        VBox.setMargin(freqArea, new Insets(0, 0, 15, 0));

        // Text preview
        Label textPreviewTitle = new Label("📝 Text Preview:");
        textPreviewTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #192a51;");

        VBox.setMargin(textPreviewTitle, new Insets(0, 0, 8, 0));

        resultsPreviewArea = new TextArea();
        resultsPreviewArea.setEditable(false);
        resultsPreviewArea.setWrapText(true);
        resultsPreviewArea.setStyle("-fx-control-inner-background: #f9f7fa; -fx-border-color: #d5c6e0; -fx-border-radius: 6; -fx-font-family: 'Consolas', monospace; -fx-font-size: 11px;");
        resultsPreviewArea.setPrefHeight(120);

        VBox.setMargin(resultsPreviewArea, new Insets(0, 0, 8, 0));

        Label previewNote = new Label("Note: First 800 characters shown");
        previewNote.setStyle("-fx-text-fill: #666; -fx-font-size: 11px; -fx-font-style: italic;");

        VBox.setMargin(previewNote, new Insets(0, 0, 15, 0));

        // Export buttons
        Label exportTitle = new Label("💾 Export Results:");
        exportTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #192a51;");

        VBox.setMargin(exportTitle, new Insets(0, 0, 8, 0));

        exportButtonsBox = new VBox(8);
        exportButtonsContainer = new HBox(10);
        exportButtonsContainer.setAlignment(Pos.CENTER_LEFT);

        exportTxtBtn = new Button("📄 Export as TXT");
        exportTxtBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-background-radius: 6; -fx-font-size: 11px;");
        exportTxtBtn.setTooltip(new Tooltip("Export results as plain text file"));

        exportCsvBtn = new Button("📊 Export as CSV");
        exportCsvBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-background-radius: 6; -fx-font-size: 11px;");
        exportCsvBtn.setTooltip(new Tooltip("Export results as CSV file (compatible with Excel)"));

        copyToClipboardBtn = new Button("📋 Copy to Clipboard");
        copyToClipboardBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-background-radius: 6; -fx-font-size: 11px;");
        copyToClipboardBtn.setTooltip(new Tooltip("Copy results to clipboard for pasting elsewhere"));

        exportButtonsContainer.getChildren().addAll(exportTxtBtn, exportCsvBtn, copyToClipboardBtn);
        exportButtonsBox.getChildren().add(exportButtonsContainer);

        VBox.setMargin(exportButtonsBox, new Insets(0, 0, 8, 0));

        // Chart (optional - hidden by default)
        setupChart();

        getChildren().addAll(
                titleBox,
                statsGrid,
                freqTitle,
                freqArea,
                textPreviewTitle,
                resultsPreviewArea,
                previewNote,
                exportTitle,
                exportButtonsBox
        );

        // Initially hide the chart
        if (frequencyChart != null) {
            frequencyChart.setVisible(false);
        }
    }

    private void setupChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        frequencyChart = new BarChart<>(xAxis, yAxis);
        frequencyChart.setTitle("Top 5 Words Frequency");
        frequencyChart.setLegendVisible(false);
        frequencyChart.setPrefHeight(200);
        frequencyChart.setVisible(false); // Hide by default
    }

    public void displayResults(Map<String, Object> results) {
        Platform.runLater(() -> {
            // Clear previous stats
            statsGrid.getChildren().clear();

            // Update title
            titleLabel.setText("📊 Analysis Results: " + results.get("fileName"));

            // Add stats rows
            int row = 0;
            addStatRow(statsGrid, row++, "Total Words:", results.get("totalWords").toString(),
                    "Total number of words in the document");
            addStatRow(statsGrid, row++, "Unique Words:", results.get("uniqueWords").toString(),
                    "Number of distinct words (excluding duplicates)");
            addStatRow(statsGrid, row++, "Characters (with spaces):", results.get("charsWithSpaces").toString(),
                    "Total characters including spaces");
            addStatRow(statsGrid, row++, "Characters (no spaces):", results.get("charsWithoutSpaces").toString(),
                    "Total characters excluding spaces");
            addStatRow(statsGrid, row++, "Sentences:", results.get("sentenceCount").toString(),
                    "Number of sentences (separated by . ! ?)");

            if (results.containsKey("paragraphCount")) {
                addStatRow(statsGrid, row++, "Paragraphs:", results.get("paragraphCount").toString(),
                        "Number of paragraphs (separated by blank lines)");
            }

            addStatRow(statsGrid, row++, "Reading Time:", results.get("readingTime") + " minutes",
                    "Estimated reading time at 200 words per minute");
            addStatRow(statsGrid, row++, "Sentiment:", results.get("sentiment").toString(),
                    "Overall emotional tone of the text");
            addStatRow(statsGrid, row++, "Avg. Word Length:", results.get("avgWordLength").toString(),
                    "Average number of characters per word");

            if (results.containsKey("longestWord")) {
                addStatRow(statsGrid, row++, "Longest Word:", results.get("longestWord").toString(),
                        "The longest word found in the text");
            }

            addStatRow(statsGrid, row++, "File Size:", results.get("fileSize").toString(),
                    "Size of the file on disk");

            // Display most frequent words
            freqArea.setText(results.get("mostFrequent").toString());

            // Display text preview
            String content = (String) results.get("fileContent");
            String previewText = content.length() > 800 ? content.substring(0, 800) + "..." : content;
            resultsPreviewArea.setText(previewText);

            // Make the view visible
            setVisible(true);
            setManaged(true);
        });
    }

    private void addStatRow(GridPane grid, int row, String label, String value, String tooltipText) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #192a51; -fx-font-size: 12px;");
        lbl.setWrapText(true);

        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #333; -fx-font-size: 12px; -fx-font-weight: bold;");
        val.setWrapText(true);

        // Create tooltip
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(250);
        Tooltip.install(lbl, tooltip);
        Tooltip.install(val, tooltip);

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    // Getter methods for buttons
    public Button getCloseButton() {
        return closeResultsBtn;
    }

    public Button getExportTxtButton() {
        return exportTxtBtn;
    }

    public Button getExportCsvButton() {
        return exportCsvBtn;
    }

    public Button getCopyToClipboardButton() {
        return copyToClipboardBtn;
    }
}