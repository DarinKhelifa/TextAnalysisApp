package com.example.textanalysisapp.view;

import com.example.textanalysisapp.controller.AnalysisManager;
import com.example.textanalysisapp.controller.FileController;
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
import java.net.URL;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.paint.Color;

import javafx.concurrent.Task;
import java.util.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

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

    private VBox previewPanel;
    private Label previewWordCount;
    private Label previewCharCount;
    private Label previewLineCount;
    private TextFlow highlightedPreview;

    private Task<Map<String, Object>> currentTask;
    private Thread currentThread;
    private Timeline progressAnimation;
    private List<String> currentTopWords = new ArrayList<>();

    private ScrollPane mainScrollPane;
    private HBox buttonContainer;
    private VBox verticalButtonBox;
    private VBox mainContent;

    // Vertical layout buttons as class fields
    private Button startBtnV;
    private Button cancelBtnV;
    private Button loadBtnV;
    private Button deleteBtnV;

    @Override
    public void start(Stage primaryStage) {
        try {
            analysisManager = new AnalysisManager();

            // Main container - BorderPane for proper layout
            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #f5e6e8;");

            // Header
            HBox headerBox = createHeader();
            root.setTop(headerBox);

            // Create main content area with ScrollPane for responsiveness
            mainContent = new VBox();
            mainContent.setPadding(new Insets(20));
            mainContent.setSpacing(0);
            mainContent.setStyle("-fx-background-color: #f5e6e8;");

            mainScrollPane = new ScrollPane(mainContent);
            mainScrollPane.setFitToWidth(true);
            mainScrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
            root.setCenter(mainScrollPane);

            // App Description
            Label descriptionLabel = new Label("Analyze your text files with powerful insights and statistics");
            descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #192a51; -fx-font-style: italic;");
            descriptionLabel.setAlignment(Pos.CENTER);
            descriptionLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(descriptionLabel, Priority.ALWAYS);

            VBox.setMargin(descriptionLabel, new Insets(0, 0, 20, 0));

            // Search field
            TextField searchField = new TextField();
            searchField.setPromptText("Search by name...");
            searchField.setPrefHeight(35);
            searchField.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-border-color: #d5c6e0; " +
                            "-fx-border-radius: 5; " +
                            "-fx-background-radius: 5; " +
                            "-fx-padding: 5 10 5 10;" +
                            "-fx-font-size: 14px;"
            );
            HBox.setHgrow(searchField, Priority.ALWAYS);

            HBox searchBox = new HBox(searchField);
            searchBox.setAlignment(Pos.CENTER);
            searchBox.setSpacing(10);
            HBox.setHgrow(searchBox, Priority.ALWAYS);

            VBox.setMargin(searchBox, new Insets(0, 0, 20, 0));

            // Buttons - Create a responsive button container
            Button loadBtn = new Button("📁 Load Files");
            loadBtn.setStyle("-fx-background-color: #d5c6e0; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            loadBtn.setMinWidth(130);

            startBtn = new Button("▶ Start Analysis");
            startBtn.setStyle("-fx-background-color: #aaa1c8; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            startBtn.setMinWidth(130);

            Button deleteBtn = new Button("🗑️ Delete");
            deleteBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            deleteBtn.setMinWidth(130);

            cancelBtn = new Button("✕ Cancel");
            cancelBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            cancelBtn.setMinWidth(130);
            cancelBtn.setDisable(true);

            // Add hover effects
            setupButtonHoverEffects(loadBtn, startBtn, deleteBtn);
            cancelBtn.setOnMouseEntered(e -> {
                if (!cancelBtn.isDisable()) {
                    cancelBtn.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
                }
            });
            cancelBtn.setOnMouseExited(e -> {
                if (!cancelBtn.isDisable()) {
                    cancelBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
                }
            });

            cancelBtn.setOnAction(e -> handleCancelAction());

            // Button container - HBox for horizontal layout
            buttonContainer = new HBox(15);
            buttonContainer.setAlignment(Pos.CENTER);
            buttonContainer.setPadding(new Insets(0, 0, 20, 0));
            buttonContainer.getChildren().addAll(loadBtn, startBtn, deleteBtn, cancelBtn);

            // Create vertical button box for small screens
            verticalButtonBox = new VBox(10);
            verticalButtonBox.setAlignment(Pos.CENTER);
            verticalButtonBox.setPadding(new Insets(0, 0, 20, 0));
            verticalButtonBox.setVisible(false);

            // Vertical layout buttons
            loadBtnV = new Button("📁 Load Files");
            loadBtnV.setStyle("-fx-background-color: #d5c6e0; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            loadBtnV.setMaxWidth(Double.MAX_VALUE);

            startBtnV = new Button("▶ Start Analysis");
            startBtnV.setStyle("-fx-background-color: #aaa1c8; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            startBtnV.setMaxWidth(Double.MAX_VALUE);

            deleteBtnV = new Button("🗑️ Delete");
            deleteBtnV.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            deleteBtnV.setMaxWidth(Double.MAX_VALUE);

            cancelBtnV = new Button("✕ Cancel");
            cancelBtnV.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            cancelBtnV.setMaxWidth(Double.MAX_VALUE);
            cancelBtnV.setDisable(true);

            loadBtnV.setOnAction(e -> loadBtn.fire());
            startBtnV.setOnAction(e -> startBtn.fire());
            deleteBtnV.setOnAction(e -> deleteBtn.fire());
            cancelBtnV.setOnAction(e -> cancelBtn.fire());

            setupButtonHoverEffects(loadBtnV, startBtnV, deleteBtnV);
            cancelBtnV.setOnMouseEntered(e -> {
                if (!cancelBtnV.isDisable()) {
                    cancelBtnV.setStyle("-fx-background-color: #ff5252; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
                }
            });
            cancelBtnV.setOnMouseExited(e -> {
                if (!cancelBtnV.isDisable()) {
                    cancelBtnV.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
                }
            });

            verticalButtonBox.getChildren().addAll(loadBtnV, startBtnV, deleteBtnV, cancelBtnV);

            // Progress bar with animation
            progressBar = new ProgressBar(0);
            progressBar.setPrefHeight(20);
            progressBar.setVisible(false);
            progressBar.setStyle("-fx-accent: #967aa1; -fx-background-radius: 10;");
            HBox.setHgrow(progressBar, Priority.ALWAYS);

            progressAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(progressBar.opacityProperty(), 0.7)),
                    new KeyFrame(Duration.seconds(0.8), new KeyValue(progressBar.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.seconds(1.6), new KeyValue(progressBar.opacityProperty(), 0.7))
            );
            progressAnimation.setCycleCount(Timeline.INDEFINITE);

            // Status label
            statusLabel = new Label("Ready to analyze");
            statusLabel.setStyle("-fx-text-fill: #192a51; -fx-font-size: 13px; -fx-font-weight: bold;");

            // Progress box container
            progressBox = new VBox(10, progressBar, statusLabel);
            progressBox.setAlignment(Pos.CENTER);
            progressBox.setPadding(new Insets(15));
            progressBox.setVisible(false);
            VBox.setVgrow(progressBox, Priority.NEVER);

            // Create preview panel
            previewPanel = createPreviewPanel();
            previewPanel.setVisible(false);
            previewPanel.setManaged(false);
            VBox.setVgrow(previewPanel, Priority.NEVER);

            // TableView
            table = new TableView<>();
            table.setStyle("-fx-control-inner-background: white; -fx-background-color: #f5e6e8; -fx-border-color: #d5c6e0; -fx-border-radius: 10;");
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
            nameCol.setMinWidth(150);

            TableColumn<FileInfo, Long> sizeCol = new TableColumn<>("Size (KB)");
            sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
            sizeCol.setMinWidth(80);

            TableColumn<FileInfo, String> dateCol = new TableColumn<>("Last Modified");
            dateCol.setCellValueFactory(new PropertyValueFactory<>("lastModified"));
            dateCol.setMinWidth(120);

            TableColumn<FileInfo, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(param -> param.getValue().statusProperty());
            statusCol.setMinWidth(100);
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
                                setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold; -fx-background-color: #E8F5E9; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
                                break;
                            case "analyzing":
                                setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold; -fx-background-color: #E3F2FD; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
                                break;
                            case "error":
                                setStyle("-fx-text-fill: #F44336; -fx-font-weight: bold; -fx-background-color: #FFEBEE; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
                                break;
                            case "pending":
                            default:
                                setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold; -fx-background-color: #FFF3E0; -fx-padding: 3 8 3 8; -fx-background-radius: 10;");
                                break;
                        }
                    }
                }
            });

            TableColumn<FileInfo, String> pathCol = new TableColumn<>("Path");
            pathCol.setCellValueFactory(new PropertyValueFactory<>("path"));
            pathCol.setMinWidth(200);

            table.getColumns().addAll(nameCol, sizeCol, dateCol, statusCol, pathCol);

            // Add selection listener
            table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    loadFilePreview(newSelection);
                } else {
                    hidePreviewPanel();
                }
            });

            // Results container
            resultsContainer = new VBox(15);
            resultsContainer.setPadding(new Insets(20));
            resultsContainer.setStyle("-fx-background-color: white; -fx-border-color: #d5c6e0; -fx-border-radius: 15; -fx-border-width: 2;");
            resultsContainer.setVisible(false);
            resultsContainer.setManaged(false);
            resultsContainer.setMinHeight(400);
            VBox.setVgrow(resultsContainer, Priority.NEVER);

            // Add all components to main content
            mainContent.getChildren().addAll(
                    descriptionLabel,
                    searchBox,
                    buttonContainer,
                    verticalButtonBox,
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
                if (files != null) {
                    for (File file : files) {
                        try {
                            String fileName = file.getName();
                            long fileSize = file.length() / 1024;
                            String lastModified = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                                    .format(file.lastModified());
                            FileInfo fileInfo = new FileInfo(fileName, fileSize, lastModified, file.getAbsolutePath());

                            // Read and store file content for preview
                            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                            fileInfo.setFileContent(content);

                            masterData.add(fileInfo);
                        } catch (Exception ex) {
                            System.err.println("Error loading file: " + file.getName() + " - " + ex.getMessage());
                        }
                    }

                    if (!files.isEmpty()) {
                        showStatusMessage("Loaded " + files.size() + " file(s) successfully", 2000);
                    }
                }
            });

            loadBtnV.setOnAction(e -> loadBtn.fire());

            // Delete Selected
            deleteBtn.setOnAction(e -> {
                ObservableList<FileInfo> selected = table.getSelectionModel().getSelectedItems();
                if (!selected.isEmpty()) {
                    masterData.removeAll(selected);
                    showStatusMessage("Removed " + selected.size() + " file(s)", 1500);
                    hidePreviewPanel();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "No file selected!");
                    alert.showAndWait();
                }
            });

            deleteBtnV.setOnAction(e -> deleteBtn.fire());

            // Start Analysis
            startBtn.setOnAction(e -> {
                FileInfo selected = table.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    File file = new File(selected.getPath());

                    if (!FileController.validateFile(file)) {
                        return;
                    }

                    // Debug logging
                    System.out.println("Starting analysis for file: " + selected.getPath());
                    System.out.println("File exists: " + file.exists());
                    System.out.println("File can read: " + file.canRead());

                    // Update status
                    selected.setStatus("analyzing");
                    table.refresh();

                    // Hide previous results and preview
                    resultsContainer.setVisible(false);
                    resultsContainer.setManaged(false);
                    resultsContainer.getChildren().clear();
                    hidePreviewPanel();

                    // Disable/enable buttons
                    startBtn.setDisable(true);
                    if (startBtnV != null) startBtnV.setDisable(true);
                    cancelBtn.setDisable(false);
                    if (cancelBtnV != null) cancelBtnV.setDisable(false);

                    // Show progress with animation
                    if (!mainContent.getChildren().contains(progressBox)) {
                        mainContent.getChildren().add(4, progressBox);
                    }
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
                            selected.setStatus("completed");
                            table.refresh();
                            stopProgressAnimation();
                            Map<String, Object> results = currentTask.getValue();
                            if (results != null) {
                                displayResults(results);
                                statusLabel.setText("Analysis complete!");
                                cancelBtn.setDisable(false);
                                if (cancelBtnV != null) cancelBtnV.setDisable(false);
                            }
                        } catch (Exception ex) {
                            selected.setStatus("error");
                            table.refresh();
                            stopProgressAnimation();
                            statusLabel.setText("Error processing results");
                            showAlert("Error", "Failed to process results: " + ex.getMessage());
                            resetUIAfterAnalysis();
                        }
                    });

                    // Handle failure
                    currentTask.setOnFailed(event -> {
                        selected.setStatus("error");
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
                        selected.setStatus("pending");
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

            startBtnV.setOnAction(e -> startBtn.fire());

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
            Scene scene = new Scene(root, 1200, 800);

            // Make layout responsive with listeners
            scene.widthProperty().addListener((obs, oldVal, newVal) -> {
                double width = newVal.doubleValue();
                if (width < 1000) {
                    buttonContainer.setVisible(false);
                    verticalButtonBox.setVisible(true);
                } else {
                    buttonContainer.setVisible(true);
                    verticalButtonBox.setVisible(false);
                }
            });

            scene.heightProperty().addListener((obs, oldVal, newVal) -> {
                double height = newVal.doubleValue();
                if (height < 700) {
                    table.setPrefHeight(200);
                } else {
                    table.setPrefHeight(300);
                }
            });

            // Load CSS
            loadExternalCSS(scene);

            // Add custom CSS
            String customCSS = """
                .root {
                    -fx-background-color: #f5e6e8;
                }
                
                .table-view {
                    -fx-font-size: 13px;
                    -fx-table-cell-border-color: transparent;
                }
                
                .table-view .column-header {
                    -fx-background-color: #d5c6e0;
                    -fx-font-weight: bold;
                    -fx-text-fill: #192a51;
                    -fx-border-color: #c0a8d0;
                }
                
                .table-view .column-header-background {
                    -fx-background-color: #d5c6e0;
                }
                
                .tooltip {
                    -fx-background-color: #192a51;
                    -fx-text-fill: white;
                    -fx-font-size: 12px;
                    -fx-font-weight: normal;
                    -fx-padding: 8 12px;
                    -fx-background-radius: 6px;
                    -fx-border-radius: 6px;
                    -fx-border-color: #d5c6e0;
                    -fx-border-width: 1px;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);
                }
                
                .button {
                    -fx-cursor: hand;
                    -fx-transition: all 0.3s;
                }
                
                .button:hover {
                    -fx-scale-x: 1.02;
                    -fx-scale-y: 1.02;
                    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);
                }
                
                .progress-bar > .track {
                    -fx-background-color: #f5e6e8;
                    -fx-background-radius: 10;
                    -fx-border-radius: 10;
                    -fx-border-color: #d5c6e0;
                }
                
                .progress-bar > .bar {
                    -fx-background-color: linear-gradient(to right, #967aa1, #aaa1c8, #967aa1);
                    -fx-background-radius: 10;
                    -fx-padding: 2px;
                }
                
                .scroll-pane {
                    -fx-background-color: transparent;
                }
                
                .scroll-pane .viewport {
                    -fx-background-color: transparent;
                }
                
                .scroll-bar:horizontal, .scroll-bar:vertical {
                    -fx-background-color: transparent;
                }
                
                .scroll-bar:horizontal .track,
                .scroll-bar:vertical .track {
                    -fx-background-color: #f5e6e8;
                    -fx-border-color: #d5c6e0;
                    -fx-background-radius: 0;
                }
                
                .scroll-bar:horizontal .thumb,
                .scroll-bar:vertical .thumb {
                    -fx-background-color: #967aa1;
                    -fx-background-radius: 5;
                }
                
                .scroll-bar .thumb:hover {
                    -fx-background-color: #6f547d;
                }
                
                .text-area {
                    -fx-font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
                    -fx-font-size: 13px;
                }
                
                .text-area .content {
                    -fx-background-color: #f9f7fa;
                    -fx-border-color: #d5c6e0;
                }
                
                .highlighted-word {
                    -fx-fill: #E91E63;
                    -fx-font-weight: bold;
                    -fx-underline: true;
                }
                
                .grid-pane {
                    -fx-hgap: 15;
                    -fx-vgap: 10;
                }
                
                .results-container {
                    -fx-background-color: white;
                    -fx-border-color: #d5c6e0;
                    -fx-border-radius: 15;
                    -fx-border-width: 2;
                }
            """;

            scene.getStylesheets().add("data:text/css," + customCSS);

            primaryStage.setTitle("VioletLens - Advanced Text Analyzer");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(700);
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
     * Create preview panel
     */
    private VBox createPreviewPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: white; -fx-border-color: #d5c6e0; -fx-border-radius: 15; -fx-border-width: 2;");

        Label previewTitle = new Label("📄 File Preview");
        previewTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #192a51;");

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(10);
        statsGrid.setPadding(new Insets(10, 0, 20, 0));

        previewWordCount = createStatLabel("Words: 0");
        previewCharCount = createStatLabel("Characters: 0");
        previewLineCount = createStatLabel("Lines: 0");

        statsGrid.add(previewWordCount, 0, 0);
        statsGrid.add(previewCharCount, 1, 0);
        statsGrid.add(previewLineCount, 2, 0);

        for (int i = 0; i < 3; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(33.33);
            col.setHgrow(Priority.ALWAYS);
            statsGrid.getColumnConstraints().add(col);
        }

        Label contentTitle = new Label("Content Preview (with highlighted frequent words):");
        contentTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #192a51; -fx-font-size: 14px;");

        highlightedPreview = new TextFlow();
        highlightedPreview.setStyle("-fx-background-color: #f9f7fa; -fx-border-color: #d5c6e0; -fx-border-radius: 10; -fx-padding: 15;");
        highlightedPreview.setMinHeight(100);
        highlightedPreview.setMaxHeight(150);

        ScrollPane previewScroll = new ScrollPane(highlightedPreview);
        previewScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        previewScroll.setFitToWidth(true);
        previewScroll.setPrefHeight(180);
        VBox.setVgrow(previewScroll, Priority.ALWAYS);

        panel.getChildren().addAll(previewTitle, statsGrid, contentTitle, previewScroll);
        VBox.setVgrow(panel, Priority.NEVER);

        return panel;
    }

    private Label createStatLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #192a51; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 15 8 15; -fx-background-color: #f0e6f5; -fx-background-radius: 8; -fx-alignment: center;");
        label.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(label, Priority.ALWAYS);
        return label;
    }

    /**
     * Load file preview with highlighted words
     */
    private void loadFilePreview(FileInfo fileInfo) {
        if (!mainContent.getChildren().contains(previewPanel)) {
            mainContent.getChildren().add(4, previewPanel);
        }

        previewPanel.setVisible(true);
        previewPanel.setManaged(true);

        String content = fileInfo.getFileContent();
        if (content == null || content.isEmpty()) {
            try {
                File file = new File(fileInfo.getPath());
                content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                fileInfo.setFileContent(content);
            } catch (Exception e) {
                content = "";
            }
        }

        int wordCount = content.split("\\s+").length;
        int charCount = content.length();
        int lineCount = content.split("\n").length;

        previewWordCount.setText("Words: " + wordCount);
        previewCharCount.setText("Characters: " + charCount);
        previewLineCount.setText("Lines: " + lineCount);

        String previewText = content.length() > 500 ? content.substring(0, 500) + "..." : content;
        updateHighlightedPreview(previewText, new ArrayList<>());
    }

    /**
     * Hide preview panel
     */
    private void hidePreviewPanel() {
        previewPanel.setVisible(false);
        previewPanel.setManaged(false);
        if (mainContent.getChildren().contains(previewPanel)) {
            mainContent.getChildren().remove(previewPanel);
        }
    }

    /**
     * Update highlighted preview with frequent words
     */
    private void updateHighlightedPreview(String text, List<String> frequentWords) {
        highlightedPreview.getChildren().clear();
        currentTopWords = frequentWords;

        if (text.isEmpty()) {
            Text noContent = new Text("No content to display");
            noContent.setStyle("-fx-fill: #666; -fx-font-style: italic;");
            highlightedPreview.getChildren().add(noContent);
            return;
        }

        String[] words = text.split("(?<=\\s)|(?=\\s)");
        for (String word : words) {
            Text textNode = new Text(word);
            String cleanWord = word.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");

            if (!cleanWord.isEmpty() && frequentWords.contains(cleanWord)) {
                textNode.getStyleClass().add("highlighted-word");
                textNode.setStyle("-fx-fill: #E91E63; -fx-font-weight: bold; -fx-underline: true;");
            } else {
                textNode.setStyle("-fx-fill: #333;");
            }

            highlightedPreview.getChildren().add(textNode);
        }
    }

    /**
     * Start progress animation
     */
    private void startProgressAnimation() {
        progressAnimation.play();
    }

    /**
     * Stop progress animation
     */
    private void stopProgressAnimation() {
        progressAnimation.stop();
        progressBar.setOpacity(1.0);
    }

    /**
     * Handle cancel action
     */
    private void handleCancelAction() {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel(true);
            if (currentThread != null && currentThread.isAlive()) {
                currentThread.interrupt();
            }
            statusLabel.setText("Cancelling analysis...");
        } else if (resultsContainer.isVisible()) {
            hideResultsAndResetUI();
        } else {
            cancelBtn.setDisable(true);
        }
    }

    /**
     * Create analysis task
     */
    private Task<Map<String, Object>> createAnalysisTask(File file, String fileName) {
        return new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                try {
                    updateProgress(0.05, 1.0);
                    updateMessage("Initializing analysis engine...");
                    Thread.sleep(200);

                    if (isCancelled()) return null;

                    updateProgress(0.15, 1.0);
                    updateMessage("Reading file content...");
                    String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
                    Thread.sleep(300);

                    if (isCancelled()) return null;
                    updateProgress(0.25, 1.0);
                    updateMessage("Analyzing basic statistics...");

                    String[] words = content.split("\\s+");
                    int totalWords = words.length;

                    Thread.sleep(200);
                    if (isCancelled()) return null;
                    updateProgress(0.35, 1.0);
                    updateMessage("Counting unique words...");

                    Set<String> uniqueWords = new HashSet<>();
                    for (String word : words) {
                        if (!word.trim().isEmpty()) {
                            uniqueWords.add(word.toLowerCase());
                        }
                    }
                    int uniqueCount = uniqueWords.size();

                    Thread.sleep(200);
                    if (isCancelled()) return null;
                    updateProgress(0.45, 1.0);
                    updateMessage("Calculating word frequency...");

                    Map<String, Integer> wordFrequency = new HashMap<>();
                    for (String word : words) {
                        word = word.toLowerCase().replaceAll("[^a-zA-Z]", "");
                        if (!word.isEmpty()) {
                            wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                        }
                    }

                    Thread.sleep(300);
                    List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(wordFrequency.entrySet());
                    sortedEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

                    if (isCancelled()) return null;
                    updateProgress(0.55, 1.0);
                    updateMessage("Processing text structure...");

                    StringBuilder frequentWords = new StringBuilder();
                    List<String> topWordsList = new ArrayList<>();
                    int limit = Math.min(10, sortedEntries.size());
                    for (int i = 0; i < limit; i++) {
                        Map.Entry<String, Integer> entry = sortedEntries.get(i);
                        frequentWords.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
                        topWordsList.add(entry.getKey());
                    }

                    Thread.sleep(200);
                    if (isCancelled()) return null;
                    updateProgress(0.65, 1.0);
                    updateMessage("Calculating advanced metrics...");

                    int charsWithSpaces = content.length();
                    int charsWithoutSpaces = content.replaceAll("\\s", "").length();
                    int sentenceCount = content.split("[.!?]+").length;
                    int paragraphCount = content.split("\\n\\s*\\n").length;
                    double readingTime = totalWords / 200.0;
                    String sentiment = calculateSentiment(content);
                    double avgWordLength = calculateAverageWordLength(content);

                    String longestWord = "";
                    for (String word : words) {
                        String cleanWord = word.replaceAll("[^a-zA-Z]", "");
                        if (cleanWord.length() > longestWord.length()) {
                            longestWord = cleanWord;
                        }
                    }

                    Thread.sleep(200);
                    if (isCancelled()) return null;
                    updateProgress(0.85, 1.0);
                    updateMessage("Finalizing results...");

                    Map<String, Object> results = new HashMap<>();
                    results.put("fileName", fileName);
                    results.put("totalWords", totalWords);
                    results.put("uniqueWords", uniqueCount);
                    results.put("charsWithSpaces", charsWithSpaces);
                    results.put("charsWithoutSpaces", charsWithoutSpaces);
                    results.put("sentenceCount", sentenceCount);
                    results.put("paragraphCount", paragraphCount);
                    results.put("readingTime", String.format("%.1f", readingTime));
                    results.put("sentiment", sentiment);
                    results.put("avgWordLength", String.format("%.2f", avgWordLength));
                    results.put("longestWord", longestWord + " (" + longestWord.length() + " chars)");
                    results.put("fileSize", file.length() / 1024 + " KB");
                    results.put("mostFrequent", frequentWords.toString());
                    results.put("topWordsList", topWordsList);
                    results.put("fileContent", content);

                    Thread.sleep(300);
                    updateProgress(1.0, 1.0);
                    updateMessage("Analysis complete!");

                    return results;

                } catch (Exception e) {
                    throw new Exception("Error analyzing file: " + e.getMessage());
                }
            }
        };
    }

    /**
     * Calculate sentiment
     */
    private String calculateSentiment(String text) {
        text = text.toLowerCase();
        String[] positiveWords = {"good", "great", "excellent", "happy", "love", "best", "nice", "perfect", "wonderful", "awesome"};
        String[] negativeWords = {"bad", "poor", "terrible", "sad", "hate", "worst", "awful", "horrible", "dislike", "angry"};

        int positive = countOccurrences(text, positiveWords);
        int negative = countOccurrences(text, negativeWords);

        if (positive > negative) return "Positive";
        else if (negative > positive) return "Negative";
        else return "Neutral";
    }

    /**
     * Calculate average word length
     */
    private double calculateAverageWordLength(String text) {
        String[] words = text.split("\\s+");
        if (words.length == 0) return 0;

        int totalChars = 0;
        for (String word : words) {
            totalChars += word.replaceAll("[^a-zA-Z]", "").length();
        }
        return (double) totalChars / words.length;
    }

    /**
     * Count word occurrences
     */
    private int countOccurrences(String text, String[] words) {
        int count = 0;
        for (String word : words) {
            int index = 0;
            while ((index = text.indexOf(word, index)) != -1) {
                count++;
                index += word.length();
            }
        }
        return count;
    }

    /**
     * Hide results and reset UI
     */
    private void hideResultsAndResetUI() {
        Platform.runLater(() -> {
            resultsContainer.setVisible(false);
            resultsContainer.setManaged(false);
            resultsContainer.getChildren().clear();
            startBtn.setDisable(false);
            if (startBtnV != null) startBtnV.setDisable(false);
            cancelBtn.setDisable(true);
            if (cancelBtnV != null) cancelBtnV.setDisable(true);
            progressBar.progressProperty().unbind();
            progressBar.setVisible(false);
            progressBox.setVisible(false);
            if (mainContent.getChildren().contains(progressBox)) {
                mainContent.getChildren().remove(progressBox);
            }
            stopProgressAnimation();
            statusLabel.setText("Ready to analyze");
            currentTask = null;
            currentThread = null;
        });
    }

    /**
     * Reset UI after analysis
     */
    private void resetUIAfterAnalysis() {
        Platform.runLater(() -> {
            startBtn.setDisable(false);
            if (startBtnV != null) startBtnV.setDisable(false);
            cancelBtn.setDisable(true);
            if (cancelBtnV != null) cancelBtnV.setDisable(true);
            progressBar.progressProperty().unbind();
            progressBar.setVisible(false);
            progressBox.setVisible(false);
            if (mainContent.getChildren().contains(progressBox)) {
                mainContent.getChildren().remove(progressBox);
            }
            stopProgressAnimation();

            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                if (!resultsContainer.isVisible()) {
                                    statusLabel.setText("Ready to analyze");
                                }
                            });
                        }
                    },
                    3000
            );

            currentTask = null;
            currentThread = null;
        });
    }

    /**
     * Show alert
     */
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * Show status message
     */
    private void showStatusMessage(String message, int duration) {
        if (!mainContent.getChildren().contains(progressBox)) {
            mainContent.getChildren().add(4, progressBox);
        }
        progressBox.setVisible(true);
        statusLabel.setText(message);

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            progressBox.setVisible(false);
                            if (mainContent.getChildren().contains(progressBox)) {
                                mainContent.getChildren().remove(progressBox);
                            }
                            statusLabel.setText("Ready to analyze");
                        });
                    }
                },
                duration
        );
    }

    /**
     * Display results with proper responsive layout
     */
    private void displayResults(Map<String, Object> results) {
        Platform.runLater(() -> {
            resultsContainer.getChildren().clear();
            resultsContainer.setVisible(true);
            resultsContainer.setManaged(true);

            mainScrollPane.setVvalue(1.0);

            Label titleLabel = new Label("📊 Analysis Results: " + results.get("fileName"));
            titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #192a51;");

            Button closeResultsBtn = new Button("✕ Close");
            closeResultsBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20 8 20; -fx-background-radius: 8;");
            closeResultsBtn.setOnAction(e -> hideResultsAndResetUI());
            closeResultsBtn.setTooltip(new Tooltip("Close results panel"));

            HBox titleBox = new HBox(titleLabel, closeResultsBtn);
            titleBox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(titleLabel, Priority.ALWAYS);
            closeResultsBtn.setAlignment(Pos.CENTER_RIGHT);

            VBox.setMargin(titleBox, new Insets(0, 0, 20, 0));

            GridPane statsGrid = new GridPane();
            statsGrid.setHgap(25);
            statsGrid.setVgap(15);
            statsGrid.setPadding(new Insets(20));
            statsGrid.setStyle("-fx-background-color: #f9f7fa; -fx-border-radius: 12; -fx-border-color: #e6d6e8; -fx-border-width: 1;");

            for (int i = 0; i < 2; i++) {
                ColumnConstraints col = new ColumnConstraints();
                col.setPercentWidth(50);
                col.setHgrow(Priority.ALWAYS);
                statsGrid.getColumnConstraints().add(col);
            }

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

            VBox.setMargin(statsGrid, new Insets(0, 0, 20, 0));

            Label freqTitle = new Label("🔤 Most Frequent Words:");
            freqTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #192a51;");

            VBox.setMargin(freqTitle, new Insets(0, 0, 10, 0));

            TextArea freqArea = new TextArea(results.get("mostFrequent").toString());
            freqArea.setEditable(false);
            freqArea.setWrapText(true);
            freqArea.setPrefHeight(100);
            freqArea.setStyle("-fx-control-inner-background: white; -fx-border-color: #d5c6e0; -fx-border-radius: 8; -fx-font-family: 'Consolas', monospace;");

            VBox.setMargin(freqArea, new Insets(0, 0, 20, 0));

            Label textPreviewTitle = new Label("📝 Text Preview (Highlighted Frequent Words):");
            textPreviewTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #192a51;");

            VBox.setMargin(textPreviewTitle, new Insets(0, 0, 10, 0));

            String content = (String) results.get("fileContent");
            String previewText = content.length() > 1000 ? content.substring(0, 1000) + "..." : content;

            List<String> topWords = (List<String>) results.get("topWordsList");
            updateHighlightedPreview(previewText, topWords != null ? topWords : new ArrayList<>());

            ScrollPane textPreviewScroll = new ScrollPane(highlightedPreview);
            textPreviewScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            textPreviewScroll.setFitToWidth(true);
            textPreviewScroll.setPrefHeight(200);

            VBox.setMargin(textPreviewScroll, new Insets(0, 0, 10, 0));

            Label previewNote = new Label("Note: Frequent words are highlighted in pink and bold");
            previewNote.setStyle("-fx-text-fill: #666; -fx-font-size: 12px; -fx-font-style: italic;");

            VBox.setMargin(previewNote, new Insets(0, 0, 20, 0));

            Label exportTitle = new Label("💾 Export Results:");
            exportTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #192a51;");

            VBox.setMargin(exportTitle, new Insets(0, 0, 10, 0));

            HBox exportButtonsBox = new HBox(15);
            exportButtonsBox.setAlignment(Pos.CENTER_LEFT);

            Button exportTxtBtn = new Button("📄 Export as TXT");
            exportTxtBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 8;");
            exportTxtBtn.setOnAction(e -> exportResults(results, "txt"));
            exportTxtBtn.setTooltip(new Tooltip("Export results as plain text file"));

            Button exportCsvBtn = new Button("📊 Export as CSV");
            exportCsvBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 8;");
            exportCsvBtn.setOnAction(e -> exportResults(results, "csv"));
            exportCsvBtn.setTooltip(new Tooltip("Export results as CSV file (compatible with Excel)"));

            Button copyToClipboardBtn = new Button("📋 Copy to Clipboard");
            copyToClipboardBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20 10 20; -fx-background-radius: 8;");
            copyToClipboardBtn.setOnAction(e -> copyResultsToClipboard(results));
            copyToClipboardBtn.setTooltip(new Tooltip("Copy results to clipboard for pasting elsewhere"));

            exportButtonsBox.getChildren().addAll(exportTxtBtn, exportCsvBtn, copyToClipboardBtn);

            VBox.setMargin(exportButtonsBox, new Insets(0, 0, 10, 0));

            resultsContainer.getChildren().addAll(
                    titleBox,
                    statsGrid,
                    freqTitle,
                    freqArea,
                    textPreviewTitle,
                    textPreviewScroll,
                    previewNote,
                    exportTitle,
                    exportButtonsBox
            );
        });
    }

    /**
     * Add a row to statistics grid with tooltip
     */
    private void addStatRow(GridPane grid, int row, String label, String value, String tooltipText) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #192a51; -fx-font-size: 14px;");
        lbl.setWrapText(true);

        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #333; -fx-font-size: 14px; -fx-font-weight: bold;");
        val.setWrapText(true);

        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setWrapText(true);
        tooltip.setMaxWidth(300);
        Tooltip.install(lbl, tooltip);
        Tooltip.install(val, tooltip);

        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    /**
     * Export results to different formats
     */
    private void exportResults(Map<String, Object> results, String format) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Analysis Results");

        switch (format.toLowerCase()) {
            case "txt":
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Text Files", "*.txt")
                );
                fileChooser.setInitialFileName("analysis_results_" + results.get("fileName") + ".txt");
                break;
            case "csv":
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("CSV Files", "*.csv")
                );
                fileChooser.setInitialFileName("analysis_results_" + results.get("fileName") + ".csv");
                break;
        }

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                String content = "";

                if (format.equalsIgnoreCase("txt")) {
                    content = generateTextExport(results);
                } else if (format.equalsIgnoreCase("csv")) {
                    content = generateCsvExport(results);
                }

                java.nio.file.Files.write(file.toPath(), content.getBytes());

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
     * Generate text export content
     */
    private String generateTextExport(Map<String, Object> results) {
        StringBuilder content = new StringBuilder();
        content.append("=".repeat(50)).append("\n");
        content.append("TEXT ANALYSIS RESULTS\n");
        content.append("=".repeat(50)).append("\n\n");
        content.append("File: ").append(results.get("fileName")).append("\n");
        content.append("Analysis Date: ").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n\n");

        content.append("STATISTICS\n");
        content.append("-".repeat(30)).append("\n");
        content.append("Total Words: ").append(results.get("totalWords")).append("\n");
        content.append("Unique Words: ").append(results.get("uniqueWords")).append("\n");
        content.append("Characters (with spaces): ").append(results.get("charsWithSpaces")).append("\n");
        content.append("Characters (no spaces): ").append(results.get("charsWithoutSpaces")).append("\n");
        content.append("Sentences: ").append(results.get("sentenceCount")).append("\n");

        if (results.containsKey("paragraphCount")) {
            content.append("Paragraphs: ").append(results.get("paragraphCount")).append("\n");
        }

        content.append("Reading Time: ").append(results.get("readingTime")).append(" minutes\n");
        content.append("Sentiment: ").append(results.get("sentiment")).append("\n");
        content.append("Average Word Length: ").append(results.get("avgWordLength")).append("\n");

        if (results.containsKey("longestWord")) {
            content.append("Longest Word: ").append(results.get("longestWord")).append("\n");
        }

        content.append("File Size: ").append(results.get("fileSize")).append("\n\n");

        content.append("MOST FREQUENT WORDS\n");
        content.append("-".repeat(30)).append("\n");
        content.append(results.get("mostFrequent")).append("\n\n");

        content.append("=".repeat(50)).append("\n");
        content.append("END OF REPORT\n");
        content.append("=".repeat(50)).append("\n");

        return content.toString();
    }

    /**
     * Generate CSV export content
     */
    private String generateCsvExport(Map<String, Object> results) {
        StringBuilder content = new StringBuilder();

        content.append("Metric,Value\n");
        content.append("File Name,").append(escapeCsv(results.get("fileName").toString())).append("\n");
        content.append("Total Words,").append(results.get("totalWords")).append("\n");
        content.append("Unique Words,").append(results.get("uniqueWords")).append("\n");
        content.append("Characters (with spaces),").append(results.get("charsWithSpaces")).append("\n");
        content.append("Characters (no spaces),").append(results.get("charsWithoutSpaces")).append("\n");
        content.append("Sentences,").append(results.get("sentenceCount")).append("\n");

        if (results.containsKey("paragraphCount")) {
            content.append("Paragraphs,").append(results.get("paragraphCount")).append("\n");
        }

        content.append("Reading Time,").append(results.get("readingTime")).append(" minutes\n");
        content.append("Sentiment,").append(escapeCsv(results.get("sentiment").toString())).append("\n");
        content.append("Average Word Length,").append(results.get("avgWordLength")).append("\n");

        if (results.containsKey("longestWord")) {
            content.append("Longest Word,").append(escapeCsv(results.get("longestWord").toString())).append("\n");
        }

        content.append("File Size,").append(results.get("fileSize")).append("\n\n");

        content.append("Most Frequent Words\n");
        content.append("Word,Frequency\n");

        String frequentWords = results.get("mostFrequent").toString();
        String[] lines = frequentWords.split("\n");
        for (String line : lines) {
            if (line.contains(":")) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String word = parts[0].trim();
                    String freq = parts[1].trim().replace(")", "").replace("(", "");
                    content.append(escapeCsv(word)).append(",").append(freq).append("\n");
                }
            }
        }

        return content.toString();
    }

    /**
     * Escape CSV special characters
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Copy results to clipboard
     */
    private void copyResultsToClipboard(Map<String, Object> results) {
        try {
            String textContent = generateTextExport(results);
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(textContent);
            clipboard.setContent(content);

            showStatusMessage("Results copied to clipboard!", 2000);
        } catch (Exception e) {
            showAlert("Clipboard Error", "Failed to copy to clipboard: " + e.getMessage());
        }
    }

    /**
     * Setup button hover effects
     */
    private void setupButtonHoverEffects(Button loadBtn, Button startBtn, Button deleteBtn) {
        loadBtn.setOnMouseEntered(e -> loadBtn.setStyle("-fx-background-color: #c0a8d0; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"));
        loadBtn.setOnMouseExited(e -> loadBtn.setStyle("-fx-background-color: #d5c6e0; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;"));

        startBtn.setOnMouseEntered(e -> startBtn.setStyle("-fx-background-color: #8f84b3; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"));
        startBtn.setOnMouseExited(e -> startBtn.setStyle("-fx-background-color: #aaa1c8; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;"));

        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #6f547d; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 3);"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;"));
    }

    /**
     * Create header
     */
    private HBox createHeader() {
        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(15));
        headerBox.setStyle("-fx-background-color: #f5e6e8; -fx-border-color: #d5c6e0; -fx-border-width: 0 0 2 0;");

        ImageView logoView = new ImageView();
        try {
            URL logoUrl = getClass().getResource("/images/logo.png");
            if (logoUrl != null) {
                Image logoImage = new Image(logoUrl.toExternalForm());
                logoView.setImage(logoImage);
                logoView.setFitWidth(50);
                logoView.setPreserveRatio(true);
            } else {
                Text logoText = new Text("🔍");
                logoText.setStyle("-fx-font-size: 32px;");
                headerBox.getChildren().add(logoText);
            }
        } catch (Exception e) {
            Text logoText = new Text("🔍");
            logoText.setStyle("-fx-font-size: 32px;");
            headerBox.getChildren().add(logoText);
        }

        VBox titleBox = new VBox(5);
        Label appNameLabel = new Label("VioletLens");
        appNameLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #192a51;");

        Label taglineLabel = new Label("Advanced Text Analysis Tool");
        taglineLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6f547d; -fx-font-style: italic;");

        titleBox.getChildren().addAll(appNameLabel, taglineLabel);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        headerBox.getChildren().addAll(logoView, titleBox);
        return headerBox;
    }

    /**
     * Load external CSS
     */
    private void loadExternalCSS(Scene scene) {
        try {
            URL cssUrl = getClass().getResource("/CSS/Style.css");
            if (cssUrl == null) {
                cssUrl = getClass().getClassLoader().getResource("CSS/Style.css");
            }

            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
        } catch (Exception e) {
            // Ignore CSS errors
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}