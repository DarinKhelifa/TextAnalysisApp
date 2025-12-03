package com.example.textanalysisapp.view;

import com.example.textanalysisapp.controller.AnalysisManager;
import com.example.textanalysisapp.controller.FileController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;
import java.net.URL;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;

// Add these imports for Sprint 2
import javafx.concurrent.Task;
import java.util.*;

public class HelloApplication extends Application {

    // Add these fields for Sprint 2
    private ProgressBar progressBar;
    private Button cancelBtn;
    private Label statusLabel;
    private VBox resultsContainer;
    private VBox progressBox;
    private AnalysisManager analysisManager;
    private TableView<FileInfo> table;
    private ObservableList<FileInfo> masterData;

    @Override
    public void start(Stage primaryStage) {
        // Initialize AnalysisManager
        analysisManager = new AnalysisManager();

        // Header with Logo and App Name
        HBox headerBox = createHeader();

        // App Description
        Label descriptionLabel = new Label("Analyze your text files with powerful insights and statistics");
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #192a51; -fx-font-style: italic;");
        descriptionLabel.setAlignment(Pos.CENTER);
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);

        // Buttons
        Button loadBtn = new Button("Load Files");
        loadBtn.setStyle("-fx-background-color: #d5c6e0; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8;");
        loadBtn.setPrefSize(140, 40);

        Button startBtn = new Button("Start Analysis");
        startBtn.setStyle("-fx-background-color: #aaa1c8; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8;");
        startBtn.setPrefSize(140, 40);

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8;");
        deleteBtn.setPrefSize(140, 40);

        // Add Sprint 2: Cancel button
        cancelBtn = new Button("Cancel Analysis");
        cancelBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8;");
        cancelBtn.setPrefSize(140, 40);
        cancelBtn.setDisable(true);

        // Add hover effects
        setupButtonHoverEffects(loadBtn, startBtn, deleteBtn);

        // Add hover effect for cancel button
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;"));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8;"));

        // Add Sprint 2: Progress bar
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setVisible(false);
        progressBar.setStyle("-fx-accent: #967aa1; -fx-pref-height: 20px;");

        // Add Sprint 2: Status label
        statusLabel = new Label("Ready to analyze");
        statusLabel.setStyle("-fx-text-fill: #192a51; -fx-font-size: 13px; -fx-font-weight: bold;");

        // Add Sprint 2: Progress box container
        progressBox = new VBox(10, progressBar, statusLabel);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new Insets(10));
        progressBox.setVisible(false);

        // Add Sprint 2: Results container
        resultsContainer = new VBox(10);
        resultsContainer.setPadding(new Insets(15));
        resultsContainer.setStyle("-fx-background-color: white; -fx-border-color: #d5c6e0; -fx-border-radius: 10; -fx-border-width: 2;");
        resultsContainer.setVisible(false);
        resultsContainer.setPrefHeight(250);

        // TableView
        table = new TableView<>();
        table.setStyle("-fx-control-inner-background: #f5e6e8; -fx-background-color: #f5e6e8;");
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        masterData = FXCollections.observableArrayList();

        // Columns
        TableColumn<FileInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);

        TableColumn<FileInfo, Long> sizeCol = new TableColumn<>("Size (KB)");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeCol.setPrefWidth(80);

        TableColumn<FileInfo, String> dateCol = new TableColumn<>("Last Modified");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("lastModified"));
        dateCol.setPrefWidth(120);

        TableColumn<FileInfo, String> pathCol = new TableColumn<>("Path");
        pathCol.setCellValueFactory(new PropertyValueFactory<>("path"));
        pathCol.setPrefWidth(250);

        table.getColumns().addAll(nameCol, sizeCol, dateCol, pathCol);

        // FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.md", "*.java", "*.xml", "*.json", "*.csv")
        );

        // Load Files
        loadBtn.setOnAction(e -> {
            List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    long fileSize = file.length() / 1024;
                    String lastModified = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                            .format(file.lastModified());
                    masterData.add(new FileInfo(fileName, fileSize, lastModified, file.getAbsolutePath()));
                }

                // Show success message if files were loaded
                if (!files.isEmpty()) {
                    statusLabel.setText("Loaded " + files.size() + " file(s) successfully");
                    progressBox.setVisible(true);
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    javafx.application.Platform.runLater(() -> progressBox.setVisible(false));
                                }
                            },
                            2000
                    );
                }
            }
        });

        // Delete Selected
        deleteBtn.setOnAction(e -> {
            ObservableList<FileInfo> selected = table.getSelectionModel().getSelectedItems();
            if (!selected.isEmpty()) {
                masterData.removeAll(selected);
                statusLabel.setText("Removed " + selected.size() + " file(s)");
                progressBox.setVisible(true);
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                javafx.application.Platform.runLater(() -> progressBox.setVisible(false));
                            }
                        },
                        1500
                );
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "No file selected!");
                alert.showAndWait();
            }
        });

        // Add Sprint 2: Start Analysis with progress tracking - CLEAN VERSION
        startBtn.setOnAction(e -> {
            FileInfo selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                File file = new File(selected.getPath());

                // Validate file
                if (!FileController.validateFile(file)) {
                    return;
                }

                // Hide previous results
                resultsContainer.setVisible(false);

                // Create and start analysis task using AnalysisManager
                Task<Map<String, Object>> task = analysisManager.createAnalysisTask(
                        file, startBtn, cancelBtn, progressBar, statusLabel
                );

                // Setup task handlers with callback for results
                analysisManager.setupTaskHandlers(
                        task, startBtn, cancelBtn, progressBar, statusLabel,
                        new AnalysisManager.AnalysisResultCallback() {
                            @Override
                            public void onAnalysisComplete(Map<String, Object> results) {
                                displayResults(results);
                                statusLabel.setText("Analysis complete!");
                            }

                            @Override
                            public void onAnalysisFailed(String errorMessage) {
                                statusLabel.setText("Analysis failed");
                            }

                            @Override
                            public void onAnalysisCancelled() {
                                statusLabel.setText("Analysis cancelled");
                            }
                        }
                );

                // Start the task
                new Thread(task).start();

            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a file to analyze first!");
                alert.showAndWait();
            }
        });

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name...");
        searchField.setPrefWidth(200);
        searchField.setPrefHeight(25);
        searchField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #d5c6e0; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-padding: 3;"
        );

        // Search bar
        HBox searchBox = new HBox(searchField);
        searchBox.setAlignment(Pos.CENTER);
        searchBox.setPadding(new Insets(5));

        FilteredList<FileInfo> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(file -> {
                if (newVal == null || newVal.isEmpty()) return true;
                return file.getName().toLowerCase().contains(newVal.toLowerCase());
            });
        });

        SortedList<FileInfo> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        // Layout
        HBox buttonsBox = new HBox(20, loadBtn, startBtn, deleteBtn, cancelBtn);
        buttonsBox.setPadding(new Insets(15, 0, 15, 0));
        buttonsBox.setAlignment(Pos.CENTER);

        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: #f5e6e8;");
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(
                headerBox,
                descriptionLabel,
                searchBox,
                buttonsBox,
                progressBox,
                table,
                resultsContainer
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        Scene scene = new Scene(layout, 1000, 700);
        loadExternalCSS(scene);

        primaryStage.setTitle("VioletLens - Text Analyzer (Sprint 2)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Display analysis results in the results container
     */
    private void displayResults(Map<String, Object> results) {
        resultsContainer.getChildren().clear();

        // Title
        Label titleLabel = new Label("Analysis Results: " + results.get("fileName"));
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #192a51;");

        // Create a grid for statistics
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(10);
        statsGrid.setPadding(new Insets(15));
        statsGrid.setStyle("-fx-background-color: #f9f7fa; -fx-border-radius: 8;");

        // Add ALL statistics to the grid
        int row = 0;

        // Total Words
        if (results.containsKey("totalWords")) {
            addStatRow(statsGrid, row++, "Total Words:", results.get("totalWords").toString());
        }

        // Unique Words
        if (results.containsKey("uniqueWords")) {
            addStatRow(statsGrid, row++, "Unique Words:", results.get("uniqueWords").toString());
        }

        // Character Count (with spaces)
        if (results.containsKey("charsWithSpaces")) {
            addStatRow(statsGrid, row++, "Characters (with spaces):", results.get("charsWithSpaces").toString());
        }

        // Character Count (without spaces)
        if (results.containsKey("charsWithoutSpaces")) {
            addStatRow(statsGrid, row++, "Characters (no spaces):", results.get("charsWithoutSpaces").toString());
        }

        // Sentence Count
        if (results.containsKey("sentenceCount")) {
            addStatRow(statsGrid, row++, "Sentences:", results.get("sentenceCount").toString());
        }

        // Reading Time
        if (results.containsKey("readingTime")) {
            addStatRow(statsGrid, row++, "Reading Time:", results.get("readingTime") + " minutes");
        }

        // Sentiment
        if (results.containsKey("sentiment")) {
            addStatRow(statsGrid, row++, "Sentiment:", results.get("sentiment").toString());
        }

        // Average Word Length
        if (results.containsKey("avgWordLength")) {
            addStatRow(statsGrid, row++, "Avg. Word Length:", results.get("avgWordLength").toString());
        }

        // File Size
        if (results.containsKey("fileSize")) {
            addStatRow(statsGrid, row++, "File Size:", results.get("fileSize").toString());
        }

        // Most Frequent Words Section
        if (results.containsKey("mostFrequent")) {
            Label freqTitle = new Label("Most Frequent Words:");
            freqTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #192a51; -fx-padding: 10 0 5 0;");

            TextArea freqArea = new TextArea(results.get("mostFrequent").toString());
            freqArea.setEditable(false);
            freqArea.setWrapText(true);
            freqArea.setPrefHeight(80);
            freqArea.setStyle("-fx-control-inner-background: white; -fx-border-color: #d5c6e0;");

            // Export button
            Button exportBtn = new Button("Export Results");
            exportBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15;");
            exportBtn.setOnAction(e -> exportResults(results));

            HBox buttonBox = new HBox(exportBtn);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(10, 0, 0, 0));

            resultsContainer.getChildren().addAll(
                    titleLabel,
                    statsGrid,
                    freqTitle,
                    freqArea,
                    buttonBox
            );
        } else {
            // If no frequency data, just show basic stats
            resultsContainer.getChildren().addAll(titleLabel, statsGrid);
        }

        resultsContainer.setVisible(true);
    }

    /**
     * Export results to a text file
     */
    private void exportResults(Map<String, Object> results) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Analysis Results");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );
        fileChooser.setInitialFileName("analysis_results_" + results.get("fileName") + ".txt");

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                StringBuilder content = new StringBuilder();
                content.append("=== Text Analysis Results ===\n");
                content.append("File: ").append(results.get("fileName")).append("\n\n");

                content.append("Statistics:\n");
                content.append("- Total Words: ").append(results.get("totalWords")).append("\n");
                content.append("- Unique Words: ").append(results.get("uniqueWords")).append("\n");
                content.append("- Characters (with spaces): ").append(results.get("charsWithSpaces")).append("\n");
                content.append("- Characters (no spaces): ").append(results.get("charsWithoutSpaces")).append("\n");
                content.append("- Sentences: ").append(results.get("sentenceCount")).append("\n");
                content.append("- Reading Time: ").append(results.get("readingTime")).append(" minutes\n");
                content.append("- Average Word Length: ").append(results.get("avgWordLength")).append("\n");
                content.append("- Sentiment: ").append(results.get("sentiment")).append("\n");
                content.append("- File Size: ").append(results.get("fileSize")).append("\n\n");

                content.append("Most Frequent Words:\n");
                content.append(results.get("mostFrequent")).append("\n");

                java.nio.file.Files.write(file.toPath(), content.toString().getBytes());

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export Successful");
                alert.setHeaderText("Results exported successfully!");
                alert.setContentText("Saved to: " + file.getAbsolutePath());
                alert.showAndWait();

            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Export Error");
                alert.setHeaderText("Failed to export results");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    /**
     * Add a row to the statistics grid
     */
    private void addStatRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #192a51; -fx-min-width: 150;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #333;");

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    // Rest of your existing methods remain unchanged...
    private void setupButtonHoverEffects(Button loadBtn, Button startBtn, Button deleteBtn) {
        // Load button hover effect
        loadBtn.setOnMouseEntered(e -> loadBtn.setStyle("-fx-background-color: #c0a8d0; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;"));
        loadBtn.setOnMouseExited(e -> loadBtn.setStyle("-fx-background-color: #d5c6e0; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8;"));

        // Start button hover effect
        startBtn.setOnMouseEntered(e -> startBtn.setStyle("-fx-background-color: #8f84b3; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;"));
        startBtn.setOnMouseExited(e -> startBtn.setStyle("-fx-background-color: #aaa1c8; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8;"));

        // Delete button hover effect
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #6f547d; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8; -fx-cursor: hand;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8;"));
    }

    private HBox createHeader() {
        ImageView logoView = new ImageView();
        try {
            URL logoUrl = getClass().getResource("/images/logo.png");
            if (logoUrl == null) {
                logoUrl = getClass().getResource("images/logo.png");
            }
            if (logoUrl == null) {
                logoUrl = getClass().getClassLoader().getResource("images/logo.png");
            }

            if (logoUrl != null) {
                Image logoImage = new Image(logoUrl.toExternalForm());
                logoView.setImage(logoImage);
                logoView.setFitWidth(50);
                logoView.setPreserveRatio(true);
                System.out.println("Logo loaded successfully from: " + logoUrl);
            } else {
                Text logoText = new Text("VL");
                logoText.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-fill: #192a51;");

                HBox headerBox = new HBox(10);
                headerBox.setAlignment(Pos.CENTER);
                headerBox.getChildren().addAll(logoText, createAppNameLabel());
                headerBox.setPadding(new Insets(15));
                headerBox.setStyle("-fx-background-color: #f5e6e8; -fx-border-color: #d5c6e0; -fx-border-width: 0 0 1 0;");
                return headerBox;
            }
        } catch (Exception e) {
            System.out.println("Error loading logo: " + e.getMessage());
        }

        Label appNameLabel = createAppNameLabel();

        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.getChildren().addAll(logoView, appNameLabel);
        headerBox.setPadding(new Insets(15));
        headerBox.setStyle("-fx-background-color: #f5e6e8; -fx-border-color: #d5c6e0; -fx-border-width: 0 0 1 0;");

        return headerBox;
    }

    private Label createAppNameLabel() {
        Label appNameLabel = new Label("VioletLens");
        appNameLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #192a51;");
        return appNameLabel;
    }

    private void loadExternalCSS(Scene scene) {
        try {
            URL cssUrl = getClass().getResource("/CSS/Style.css");
            if (cssUrl == null) {
                cssUrl = getClass().getClassLoader().getResource("CSS/Style.css");
            }

            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("CSS loaded successfully from: " + cssUrl);
            } else {
                System.out.println("CSS file not found. Using inline styles only.");
            }
        } catch (Exception e) {
            System.out.println("Error loading CSS: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}