package com.example.textanalysisapp.view;

import javafx.application.Application;
import javafx.scene.Scene;
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
import java.util.stream.Collectors;

public class HelloApplication extends Application {

    // Add these fields for Sprint 2
    private ProgressBar progressBar;
    private Button cancelBtn;
    private Label statusLabel;
    private VBox resultsContainer;
    private VBox progressBox;

    @Override
    public void start(Stage primaryStage) {
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
        cancelBtn = new Button("Cancel");
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
        progressBar.setPrefWidth(300);
        progressBar.setVisible(false);
        progressBar.setStyle("-fx-accent: #967aa1;");

        // Add Sprint 2: Status label
        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #192a51; -fx-font-size: 13px;");

        // Add Sprint 2: Progress box container
        progressBox = new VBox(10, progressBar, statusLabel);
        progressBox.setAlignment(Pos.CENTER);
        progressBox.setPadding(new Insets(10));
        progressBox.setVisible(false);

        // Add Sprint 2: Results container
        resultsContainer = new VBox(10);
        resultsContainer.setPadding(new Insets(15));
        resultsContainer.setStyle("-fx-background-color: white; -fx-border-color: #d5c6e0; -fx-border-radius: 10;");
        resultsContainer.setVisible(false);

        // TableView
        TableView<FileInfo> table = new TableView<>();
        table.setStyle("-fx-control-inner-background: #f5e6e8; -fx-background-color: #f5e6e8;");
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ObservableList<FileInfo> masterData = FXCollections.observableArrayList();

        // Columns
        TableColumn<FileInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<FileInfo, Long> sizeCol = new TableColumn<>("Size (KB)");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));

        TableColumn<FileInfo, String> dateCol = new TableColumn<>("Last Modified");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("lastModified"));

        TableColumn<FileInfo, String> pathCol = new TableColumn<>("Path");
        pathCol.setCellValueFactory(new PropertyValueFactory<>("path"));
        pathCol.setPrefWidth(250);

        table.getColumns().addAll(nameCol, sizeCol, dateCol, pathCol);

        // FileChooser
        FileChooser fileChooser = new FileChooser();

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
            }
        });

        // Delete Selected
        deleteBtn.setOnAction(e -> {
            ObservableList<FileInfo> selected = table.getSelectionModel().getSelectedItems();
            if (!selected.isEmpty()) {
                masterData.removeAll(selected);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "No file selected!");
                alert.showAndWait();
            }
        });

        // Add Sprint 2: Start Analysis with progress tracking
        startBtn.setOnAction(e -> {
            FileInfo selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // Disable buttons during analysis
                startBtn.setDisable(true);
                loadBtn.setDisable(true);
                deleteBtn.setDisable(true);
                cancelBtn.setDisable(false);

                // Show progress UI
                progressBox.setVisible(true);
                progressBar.setVisible(true);
                progressBar.setProgress(0);
                statusLabel.setText("Analyzing: " + selected.getName() + "...");

                // Create analysis task
                Task<Map<String, Object>> task = new Task<Map<String, Object>>() {
                    @Override
                    protected Map<String, Object> call() throws Exception {
                        Map<String, Object> results = new HashMap<>();
                        File file = new File(selected.getPath());

                        // Read file content
                        String content;
                        try {
                            content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                        } catch (Exception ex) {
                            throw new Exception("Cannot read file: " + ex.getMessage());
                        }

                        // Simulate progress
                        for (int i = 0; i <= 100; i += 10) {
                            updateProgress(i, 100);
                            Thread.sleep(50);
                        }

                        // Perform analysis
                        String[] words = content.split("\\s+");
                        int totalWords = words.length;
                        results.put("totalWords", totalWords);

                        Set<String> uniqueWords = Arrays.stream(words)
                                .map(String::toLowerCase)
                                .filter(word -> !word.isEmpty())
                                .collect(Collectors.toSet());
                        results.put("uniqueWords", uniqueWords.size());

                        // Find most frequent words
                        Map<String, Integer> wordFrequency = new HashMap<>();
                        for (String word : words) {
                            String cleanWord = word.toLowerCase().replaceAll("[^a-zA-Z]", "");
                            if (!cleanWord.isEmpty()) {
                                wordFrequency.put(cleanWord, wordFrequency.getOrDefault(cleanWord, 0) + 1);
                            }
                        }

                        String topWords = wordFrequency.entrySet().stream()
                                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                                .limit(5)
                                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                                .collect(Collectors.joining(", "));
                        results.put("mostFrequent", topWords.isEmpty() ? "No words found" : topWords);

                        results.put("fileName", selected.getName());
                        results.put("fileSize", selected.getSize() + " KB");

                        return results;
                    }
                };

                // Bind progress bar to task progress
                progressBar.progressProperty().bind(task.progressProperty());

                // Handle successful completion
                task.setOnSucceeded(event -> {
                    Map<String, Object> results = task.getValue();
                    displayResults(results);

                    // Reset UI
                    startBtn.setDisable(false);
                    loadBtn.setDisable(false);
                    deleteBtn.setDisable(false);
                    cancelBtn.setDisable(true);
                    statusLabel.setText("Analysis complete!");

                    // Hide progress after delay
                    new java.util.Timer().schedule(
                            new java.util.TimerTask() {
                                @Override
                                public void run() {
                                    javafx.application.Platform.runLater(() -> progressBox.setVisible(false));
                                }
                            },
                            1000
                    );
                });

                // Handle failure
                task.setOnFailed(event -> {
                    Throwable exception = task.getException();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Analysis Error");
                    alert.setHeaderText("Failed to analyze " + selected.getName());
                    alert.setContentText(exception.getMessage());
                    alert.showAndWait();

                    // Reset UI
                    startBtn.setDisable(false);
                    loadBtn.setDisable(false);
                    deleteBtn.setDisable(false);
                    cancelBtn.setDisable(true);
                    progressBox.setVisible(false);
                    statusLabel.setText("Analysis failed");
                });

                // Cancel button action
                cancelBtn.setOnAction(cancelEvent -> {
                    if (task.isRunning()) {
                        task.cancel();
                        startBtn.setDisable(false);
                        loadBtn.setDisable(false);
                        deleteBtn.setDisable(false);
                        cancelBtn.setDisable(true);
                        progressBox.setVisible(false);
                        statusLabel.setText("Analysis cancelled");
                    }
                });

                // Start the analysis in a new thread
                new Thread(task).start();

            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a file first!");
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

        Scene scene = new Scene(layout, 900, 600);
        loadExternalCSS(scene);

        primaryStage.setTitle("VioletLens - Text Analyzer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Add Sprint 2: Method to display results
    private void displayResults(Map<String, Object> results) {
        resultsContainer.getChildren().clear();

        // Title
        Label titleLabel = new Label("Analysis Results: " + results.get("fileName"));
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #192a51;");

        // Statistics
        Label statsLabel = new Label(
                "Total Words: " + results.get("totalWords") + "\n" +
                        "Unique Words: " + results.get("uniqueWords") + "\n" +
                        "File Size: " + results.get("fileSize") + "\n" +
                        "Most Frequent: " + results.get("mostFrequent")
        );
        statsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #192a51;");
        statsLabel.setPadding(new Insets(10));

        resultsContainer.getChildren().addAll(titleLabel, statsLabel);
        resultsContainer.setVisible(true);
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