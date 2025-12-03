package com.example.textanalysisapp.view;

import com.example.textanalysisapp.model.AnalysisResult;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

public class AnalysisResultView extends VBox {

    private Label titleLabel;
    private GridPane statsGrid;
    private TextArea topWordsArea;
    private BarChart<String, Number> frequencyChart;

    public AnalysisResultView() {
        setupUI();
    }

    private void setupUI() {
        setSpacing(15);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: white; -fx-border-color: #d5c6e0; -fx-border-radius: 10;");

        // Title
        titleLabel = new Label("Analysis Results");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #192a51;");

        // Stats Grid
        statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(10);
        statsGrid.setPadding(new Insets(10));

        // Most Frequent Words Area
        topWordsArea = new TextArea();
        topWordsArea.setEditable(false);
        topWordsArea.setWrapText(true);
        topWordsArea.setPrefHeight(100);
        topWordsArea.setStyle("-fx-control-inner-background: #f9f7fa;");

        // Chart
        setupChart();

        getChildren().addAll(titleLabel, statsGrid,
                new Label("Most Frequent Words:"), topWordsArea,
                new Label("Word Frequency:"), frequencyChart);
    }

    private void setupChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        frequencyChart = new BarChart<>(xAxis, yAxis);
        frequencyChart.setTitle("Top 5 Words Frequency");
        frequencyChart.setLegendVisible(false);
        frequencyChart.setPrefHeight(200);
    }

    public void displayResults(AnalysisResult result) {
        titleLabel.setText("Analysis Results: " + result.getFileName());

        // Clear previous stats
        statsGrid.getChildren().clear();

        // Add stats
        addStatRow(statsGrid, 0, "Total Words:", result.getTotalWords() + "");
        addStatRow(statsGrid, 1, "Unique Words:", result.getUniqueWords() + "");
        addStatRow(statsGrid, 2, "Characters:", result.getCharacterCount() + "");
        addStatRow(statsGrid, 3, "Reading Time:", result.getReadingTime() + " minutes");
        addStatRow(statsGrid, 4, "Sentiment:", result.getSentiment());

        // Display top words
        topWordsArea.setText(result.getMostFrequent());

        // Update chart (you'll need to parse the frequency data)
        updateChart(result.getMostFrequent());
    }

    private void addStatRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold;");
        Label val = new Label(value);

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private void updateChart(String frequencyData) {
        // Parse frequency data and update chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Example: "hello (5), world (3), ..."
        String[] items = frequencyData.split(", ");
        for (String item : items) {
            String[] parts = item.split(" \\(");
            if (parts.length == 2) {
                String word = parts[0];
                int count = Integer.parseInt(parts[1].replace(")", ""));
                series.getData().add(new XYChart.Data<>(word, count));
            }
        }

        frequencyChart.getData().clear();
        frequencyChart.getData().add(series);
    }
}