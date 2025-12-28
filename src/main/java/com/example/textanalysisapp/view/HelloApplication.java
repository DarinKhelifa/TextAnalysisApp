package com.example.textanalysisapp.view;

import com.example.textanalysisapp.controller.AnalysisManager;
import com.example.textanalysisapp.controller.FileController;
import com.example.textanalysisapp.view.AnalysisResultView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.concurrent.Task;
import java.util.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class HelloApplication extends Application {

    private ProgressBar progressBar;
    private Button cancelBtn;
    private Button startBtn;
    private Label statusLabel;
    private VBox resultsContainer;
    private VBox progressBox;
    private AnalysisManager analysisManager;
    private TableView<FileInfo> table;
    private ObservableList<FileInfo> masterData;
    private Task<Map<String, Object>> currentTask;
    private Thread currentThread;
    private Timeline progressAnimation;

    @Override
    public void start(Stage primaryStage) {
        try {
            analysisManager = new AnalysisManager();

            // Main container
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #f5e6e8;");

            // Header
            HBox headerBox = createHeader();
            root.setTop(headerBox);

            // Create main content area with ScrollPane for responsiveness
            VBox mainContent = new VBox();
            mainContent.setPadding(new Insets(20));
            mainContent.setSpacing(0);
            mainContent.setStyle("-fx-background-color: #f5e6e8;");

            ScrollPane mainScrollPane = new ScrollPane(mainContent);
            mainScrollPane.setFitToWidth(true);
            mainScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
            root.setCenter(mainScrollPane);

            // App Description
            Label descriptionLabel = new Label("Analyze your text files with powerful insights and statistics");
            descriptionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #192a51; -fx-font-style: italic;");
            descriptionLabel.setAlignment(Pos.CENTER);
            descriptionLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(descriptionLabel, Priority.ALWAYS);

            VBox.setMargin(descriptionLabel, new Insets(0, 0, 20, 0));

            // Search field
            TextField searchField = new TextField();
            searchField.setPromptText("Search by name...");
            searchField.setPrefHeight(32);
            searchField.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-border-color: #d5c6e0; " +
                            "-fx-border-radius: 5; " +
                            "-fx-background-radius: 5; " +
                            "-fx-padding: 5 10 5 10;" +
                            "-fx-font-size: 13px;"
            );
            HBox.setHgrow(searchField, Priority.ALWAYS);

            HBox searchBox = new HBox(searchField);
            searchBox.setAlignment(Pos.CENTER);
            searchBox.setSpacing(8);
            HBox.setHgrow(searchBox, Priority.ALWAYS);

            VBox.setMargin(searchBox, new Insets(0, 0, 20, 0));

            // Buttons - مع إضافة HOVER فقط
            Button loadBtn = new Button("📁 Load Files");
            loadBtn.setStyle("-fx-background-color: #d5c6e0; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            loadBtn.setMinWidth(130);

            startBtn = new Button("▶ Start Analysis");
            startBtn.setStyle("-fx-background-color: #aaa1c8; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            startBtn.setMinWidth(130);

            Button deleteBtn = new Button("🗑 Delete");
            deleteBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            deleteBtn.setMinWidth(130);

            cancelBtn = new Button("✕ Cancel");
            cancelBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            cancelBtn.setMinWidth(130);
            cancelBtn.setDisable(true);

            // ========== إضافة HOVER فقط هنا ==========
            // Hover effect for Load Button
            loadBtn.setOnMouseEntered(e -> loadBtn.setStyle("-fx-background-color: #c0a8d0; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"));
            loadBtn.setOnMouseExited(e -> loadBtn.setStyle("-fx-background-color: #d5c6e0; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;"));

            // Hover effect for Start Button
            startBtn.setOnMouseEntered(e -> startBtn.setStyle("-fx-background-color: #8f84b3; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"));
            startBtn.setOnMouseExited(e -> startBtn.setStyle("-fx-background-color: #aaa1c8; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;"));

            // Hover effect for Delete Button
            deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #6f547d; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"));
            deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;"));

            // Hover effect for Cancel Button
            cancelBtn.setOnMouseEntered(e -> {
                if (!cancelBtn.isDisable()) {
                    cancelBtn.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);");
                }
            });
            cancelBtn.setOnMouseExited(e -> {
                if (!cancelBtn.isDisable()) {
                    cancelBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
                }
            });
            // ========== نهاية إضافة HOVER ==========

            cancelBtn.setOnAction(e -> handleCancelAction());

            // Button container - HBox for horizontal layout
            HBox buttonContainer = new HBox(15);
            buttonContainer.setAlignment(Pos.CENTER);
            buttonContainer.setPadding(new Insets(0, 0, 20, 0));
            buttonContainer.getChildren().addAll(loadBtn, startBtn, deleteBtn, cancelBtn);

            // Progress bar with animation
            progressBar = new ProgressBar(0);
            progressBar.setPrefHeight(18);
            progressBar.setVisible(false);
            progressBar.setStyle("-fx-accent: #967aa1; -fx-background-radius: 8;");
            HBox.setHgrow(progressBar, Priority.ALWAYS);

            progressAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(progressBar.opacityProperty(), 0.7)),
                    new KeyFrame(Duration.seconds(0.8), new KeyValue(progressBar.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.seconds(1.6), new KeyValue(progressBar.opacityProperty(), 0.7))
            );
            progressAnimation.setCycleCount(Timeline.INDEFINITE);

            // Status label
            statusLabel = new Label("Ready to analyze");
            statusLabel.setStyle("-fx-text-fill: #192a51; -fx-font-size: 12px; -fx-font-weight: bold;");

            // Progress box container
            progressBox = new VBox(8, progressBar, statusLabel);
            progressBox.setAlignment(Pos.CENTER);
            progressBox.setPadding(new Insets(12));
            progressBox.setVisible(false);
            VBox.setVgrow(progressBox, Priority.NEVER);
            VBox.setMargin(progressBox, new Insets(0, 0, 20, 0));

            // TableView
            table = new TableView<>();
            table.setStyle("-fx-control-inner-background: white; -fx-background-color: #f5e6e8; -fx-border-color: #d5c6e0; -fx-border-radius: 8;");
            table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            masterData = FXCollections.observableArrayList();

            table.setPrefHeight(300);
            table.setMinHeight(200);
            VBox.setVgrow(table, Priority.ALWAYS);
            VBox.setMargin(table, new Insets(0, 0, 20, 0));
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

            // Columns
            TableColumn<FileInfo, String> nameCol = new TableColumn<>("Name");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameCol.setMinWidth(120);

            TableColumn<FileInfo, Long> sizeCol = new TableColumn<>("Size (KB)");
            sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
            sizeCol.setMinWidth(70);

            TableColumn<FileInfo, String> dateCol = new TableColumn<>("Last Modified");
            dateCol.setCellValueFactory(new PropertyValueFactory<>("lastModified"));
            dateCol.setMinWidth(100);

            TableColumn<FileInfo, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(param -> param.getValue().statusProperty());
            statusCol.setMinWidth(90);
            statusCol.setCellFactory(column -> new TableCell<FileInfo, String>() {
                @Override
                protected void updateItem(String status, boolean empty) {
                    super.updateItem(status, empty);
                    if (empty || status == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(status);
                        switch (status.toLowerCase()) {
                            case "completed":
                                setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-background-color: #E8F5E9; -fx-padding: 2 6 2 6; -fx-background-radius: 8;");
                                break;
                            case "analyzing":
                                setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold; -fx-background-color: #E3F2FD; -fx-padding: 2 6 2 6; -fx-background-radius: 8;");
                                break;
                            case "error":
                                setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold; -fx-background-color: #FFEBEE; -fx-padding: 2 6 2 6; -fx-background-radius: 8;");
                                break;
                            case "pending":
                            default:
                                setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold; -fx-background-color: #FFF3E0; -fx-padding: 2 6 2 6; -fx-background-radius: 8;");
                                break;
                        }
                    }
                }
            });

            TableColumn<FileInfo, String> pathCol = new TableColumn<>("Path");
            pathCol.setCellValueFactory(new PropertyValueFactory<>("path"));
            pathCol.setMinWidth(150);

            table.getColumns().addAll(nameCol, sizeCol, dateCol, statusCol, pathCol);

            // Results container
            VBox resultsContainer = new VBox();
            resultsContainer.setStyle("-fx-background-color: transparent;");
            resultsContainer.setVisible(false);
            resultsContainer.setManaged(false);
            VBox.setVgrow(resultsContainer, Priority.NEVER);

            // Create AnalysisResultView
            AnalysisResultView analysisResultView = new AnalysisResultView();
            analysisResultView.setVisible(false);
            resultsContainer.getChildren().add(analysisResultView);

            // Add all components to main content
            mainContent.getChildren().addAll(
                    descriptionLabel,
                    searchBox,
                    buttonContainer,
                    progressBox,
                    table,
                    resultsContainer
            );

            // FileChooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Text Files");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.md", "*.java", "*.xml", "*.json", "*.csv", "*.html", "*.htm"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );

            // Load Files
            loadBtn.setOnAction(e -> {
                List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
                if (files != null && !files.isEmpty()) {
                    for (File file : files) {
                        try {
                            String fileName = file.getName();
                            long fileSize = file.length() / 1024;
                            String lastModified = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                                    .format(file.lastModified());
                            FileInfo fileInfo = new FileInfo(fileName, fileSize, lastModified, file.getAbsolutePath());
                            masterData.add(fileInfo);
                        } catch (Exception ex) {
                            System.err.println("Error loading file: " + file.getName());
                        }
                    }

                    // تحديث الجدول
                    table.refresh();

                    // رسالة نجاح
                    showStatusMessage("Loaded " + files.size() + " file(s) successfully", 2000);

                    // تحديد أول ملف في الجدول
                    if (!masterData.isEmpty()) {
                        table.getSelectionModel().select(0);
                    }
                }
            });

            // Delete Selected
            deleteBtn.setOnAction(e -> {
                ObservableList<FileInfo> selected = table.getSelectionModel().getSelectedItems();
                if (!selected.isEmpty()) {
                    masterData.removeAll(selected);
                    showStatusMessage("Removed " + selected.size() + " file(s)", 1500);
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "No file selected!");
                    alert.showAndWait();
                }
            });

            // Start Analysis
            startBtn.setOnAction(e -> {
                FileInfo selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    File file = new File(selected.getPath());

                    if (!FileController.validateFile(file)) {
                        return;
                    }

                    // Update status
                    selected.setStatus("Analyzing");
                    table.refresh();

                    // Hide previous results
                    resultsContainer.setVisible(false);

                    // Disable/enable buttons
                    startBtn.setDisable(true);
                    cancelBtn.setDisable(false);

                    // Show progress with animation
                    progressBox.setVisible(true);
                    progressBar.setProgress(0);
                    statusLabel.setText("Starting analysis...");
                    progressBar.setVisible(true);
                    startProgressAnimation();

                    // Create analysis task
                    currentTask = createAnalysisTask(file, selected.getName());

                    // Bind progress
                    progressBar.progressProperty().bind(currentTask.progressProperty());

                    // Handle success
                    currentTask.setOnSucceeded(event -> {
                        try {
                            selected.setStatus("Completed");
                            table.refresh();
                            stopProgressAnimation();
                            Map<String, Object> results = currentTask.getValue();
                            if (results != null) {
                                displayResults(results, analysisResultView, resultsContainer);
                                statusLabel.setText("Analysis complete!");
                            }
                        } catch (Exception ex) {
                            selected.setStatus("Error");
                            table.refresh();
                            stopProgressAnimation();
                            statusLabel.setText("Error processing results");
                            showAlert("Error", "Failed to process results: " + ex.getMessage());
                            resetUIAfterAnalysis();
                        }
                    });

                    // Handle failure
                    currentTask.setOnFailed(event -> {
                        selected.setStatus("Error");
                        table.refresh();
                        stopProgressAnimation();
                        Throwable exception = currentTask.getException();
                        statusLabel.setText("Analysis failed");
                        showAlert("Analysis Failed",
                                "Could not analyze the file:\n" + exception.getMessage());
                        resetUIAfterAnalysis();
                    });

                    // Handle cancellation
                    currentTask.setOnCancelled(event -> {
                        selected.setStatus("Pending");
                        table.refresh();
                        stopProgressAnimation();
                        statusLabel.setText("Analysis cancelled");
                        resetUIAfterAnalysis();
                    });

                    // Start task
                    currentThread = new Thread(currentTask);
                    currentThread.setDaemon(true);
                    currentThread.start();

                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a file to analyze first!");
                    alert.showAndWait();
                }
            });

            // Search functionality
            FilteredList<FileInfo> filteredData = new FilteredList<>(masterData, p -> true);
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredData.setPredicate(file -> {
                    if (newVal == null || newVal.isEmpty()) return true;
                    String searchLower = newVal.toLowerCase();
                    return file.getName().toLowerCase().contains(searchLower) ||
                            file.getPath().toLowerCase().contains(searchLower);
                });
            });

            SortedList<FileInfo> sortedData = new SortedList<>(filteredData);
            sortedData.comparatorProperty().bind(table.comparatorProperty());
            table.setItems(sortedData);

            // Create scene with responsive size
            Scene scene = new Scene(root, 1100, 750);

            // إضافة CSS لـHover للجدول وأزرار النتائج
            String hoverCSS = """
                /* Hover effect for table rows */
                .table-row-cell:hover {
                    -fx-background-color: #f0e6f5;
                    -fx-cursor: hand;
                }
                
                /* Hover effect for all buttons */
                .button:hover {
                    -fx-scale-x: 1.02;
                    -fx-scale-y: 1.02;
                }
                
                /* Smooth transitions */
                .button {
                    -fx-transition: all 0.2s ease;
                }
            """;

            scene.getStylesheets().add("data:text/css," + hoverCSS);

            primaryStage.setTitle("VioletLens - Advanced Text Analyzer");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Application Error");
            alert.setHeaderText("Failed to start application");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Display results
     */
    private void displayResults(Map<String, Object> results, AnalysisResultView analysisResultView, VBox resultsContainer) {
        Platform.runLater(() -> {
            resultsContainer.setVisible(true);
            resultsContainer.setManaged(true);

            // Use AnalysisResultView to display results
            analysisResultView.displayResults(results);

            // Set up button actions with HOVER
            Button closeBtn = analysisResultView.getCloseButton();
            if (closeBtn != null) {
                closeBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 15 6 15; -fx-background-radius: 6;");
                closeBtn.setOnMouseEntered(e -> closeBtn.setStyle("-fx-background-color: #6f547d; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 15 6 15; -fx-background-radius: 6; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"));
                closeBtn.setOnMouseExited(e -> closeBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 6 15 6 15; -fx-background-radius: 6;"));
                closeBtn.setOnAction(e -> {
                    resultsContainer.setVisible(false);
                    resetUIAfterAnalysis();
                });
            }

            Button exportTxtBtn = analysisResultView.getExportTxtButton();
            if (exportTxtBtn != null) {
                exportTxtBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-background-radius: 6;");
                exportTxtBtn.setOnMouseEntered(e -> exportTxtBtn.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-background-radius: 6; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"));
                exportTxtBtn.setOnMouseExited(e -> exportTxtBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-background-radius: 6;"));
                exportTxtBtn.setOnAction(e -> exportResults(results, "txt"));
            }

            Button exportCsvBtn = analysisResultView.getExportCsvButton();
            if (exportCsvBtn != null) {
                exportCsvBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-background-radius: 6;");
                exportCsvBtn.setOnMouseEntered(e -> exportCsvBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-background-radius: 6; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"));
                exportCsvBtn.setOnMouseExited(e -> exportCsvBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-background-radius: 6;"));
                exportCsvBtn.setOnAction(e -> exportResults(results, "csv"));
            }

            Button copyBtn = analysisResultView.getCopyToClipboardButton();
            if (copyBtn != null) {
                copyBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-background-radius: 6;");
                copyBtn.setOnMouseEntered(e -> copyBtn.setStyle("-fx-background-color: #F57C00; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-background-radius: 6; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"));
                copyBtn.setOnMouseExited(e -> copyBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-background-radius: 6;"));
                copyBtn.setOnAction(e -> copyResultsToClipboard(results));
            }
        });
    }

    // ========== باقي الدوال تبقى كما هي بدون تغيير ==========

    private Task<Map<String, Object>> createAnalysisTask(File file, String fileName) {
        return new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                // نفس الكود الأصلي...
                updateProgress(0.1, 1.0);
                updateMessage("Reading file content...");

                String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));

                updateProgress(0.3, 1.0);
                updateMessage("Analyzing text...");

                String[] words = content.split("\\s+");
                int totalWords = words.length;

                Set<String> uniqueWords = new HashSet<>();
                Map<String, Integer> wordFrequency = new HashMap<>();

                for (String word : words) {
                    String cleanWord = word.toLowerCase().replaceAll("[^a-z]", "");
                    if (!cleanWord.isEmpty()) {
                        uniqueWords.add(cleanWord);
                        wordFrequency.put(cleanWord, wordFrequency.getOrDefault(cleanWord, 0) + 1);
                    }
                }

                updateProgress(0.7, 1.0);

                // Sort by frequency
                List<Map.Entry<String, Integer>> sorted = new ArrayList<>(wordFrequency.entrySet());
                sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

                StringBuilder frequentWords = new StringBuilder();
                int limit = Math.min(5, sorted.size());
                for (int i = 0; i < limit; i++) {
                    Map.Entry<String, Integer> entry = sorted.get(i);
                    frequentWords.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                }

                // Basic metrics
                int charsWithSpaces = content.length();
                int charsWithoutSpaces = content.replaceAll("\\s", "").length();
                int sentenceCount = content.split("[.!?]+").length;
                double readingTime = totalWords / 200.0;

                updateProgress(1.0, 1.0);

                Map<String, Object> results = new HashMap<>();
                results.put("fileName", fileName);
                results.put("totalWords", totalWords);
                results.put("uniqueWords", uniqueWords.size());
                results.put("charsWithSpaces", charsWithSpaces);
                results.put("charsWithoutSpaces", charsWithoutSpaces);
                results.put("sentenceCount", sentenceCount);
                results.put("readingTime", String.format("%.1f", readingTime));
                results.put("mostFrequent", frequentWords.toString());
                results.put("fileContent", content);

                return results;
            }
        };
    }

    private void exportResults(Map<String, Object> results, String format) {
        // نفس الكود الأصلي...
    }

    private String generateTextExport(Map<String, Object> results) {
        // نفس الكود الأصلي...
        return "";
    }

    private String generateCsvExport(Map<String, Object> results) {
        // نفس الكود الأصلي...
        return "";
    }

    private void copyResultsToClipboard(Map<String, Object> results) {
        // نفس الكود الأصلي...
    }

    private void handleCancelAction() {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel(true);
            if (currentThread != null && currentThread.isAlive()) {
                currentThread.interrupt();
            }
            statusLabel.setText("Cancelling analysis...");
        }
    }

    private void startProgressAnimation() {
        progressAnimation.play();
    }

    private void stopProgressAnimation() {
        progressAnimation.stop();
        progressBar.setOpacity(1.0);
    }

    private void resetUIAfterAnalysis() {
        Platform.runLater(() -> {
            startBtn.setDisable(false);
            cancelBtn.setDisable(true);
            progressBar.progressProperty().unbind();
            progressBar.setVisible(false);
            progressBox.setVisible(false);
            stopProgressAnimation();
            statusLabel.setText("Ready to analyze");
            currentTask = null;
            currentThread = null;
        });
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showStatusMessage(String message, int duration) {
        Platform.runLater(() -> {
            progressBox.setVisible(true);
            statusLabel.setText(message);

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                progressBox.setVisible(false);
                                statusLabel.setText("Ready to analyze");
                            });
                        }
                    },
                    duration
            );
        });
    }

    private HBox createHeader() {
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(10, 15, 10, 15));
        headerBox.setStyle("-fx-background-color: #f5e6e8; -fx-border-color: #d5c6e0; -fx-border-width: 0 0 2 0;");

        javafx.scene.text.Text logoText = new javafx.scene.text.Text("🔍");
        logoText.setStyle("-fx-font-size: 28px;");

        VBox titleBox = new VBox(5);
        Label appNameLabel = new Label("VioletLens");
        appNameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #192a51;");

        Label taglineLabel = new Label("Advanced Text Analysis Tool");
        taglineLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6f547d; -fx-font-style: italic;");

        titleBox.getChildren().addAll(appNameLabel, taglineLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        headerBox.getChildren().addAll(logoText, titleBox);
        return headerBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}