package com.example.textanalysisapp.view;

import com.example.textanalysisapp.controller.AnalysisManager;
import com.example.textanalysisapp.controller.MultiThreadedAnalysisManager;
import com.example.textanalysisapp.controller.FileController;
import com.example.textanalysisapp.model.AnalysisResult;
import com.example.textanalysisapp.view.AnalysisResultView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.concurrent.Task;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class HelloApplication extends Application {

    private ProgressBar progressBar;
    private Button cancelBtn;
    private Button startBtn;
    private Button analyzeAllBtn;
    private Button analyzeSelectedBtn;
    private Label statusLabel;
    private VBox resultsContainer;
    private VBox progressBox;
    private HBox overallProgressBox;
    private ProgressIndicator overallProgressIndicator;
    private Label overallProgressLabel;
    private AnalysisManager analysisManager;
    private MultiThreadedAnalysisManager multiAnalysisManager;
    private TableView<FileInfo> table;
    private ObservableList<FileInfo> masterData;
    private Task<Map<String, Object>> currentTask;
    private Thread currentThread;
    private Timeline progressAnimation;
    private Map<String, Map<String, Object>> allResults;
    private boolean isMultiAnalysisMode = false;
    private final Object lock = new Object();
    private CheckBox selectAllCheckBox;

    @Override
    public void start(Stage primaryStage) {
        try {
            analysisManager = new AnalysisManager();
            multiAnalysisManager = new MultiThreadedAnalysisManager();
            allResults = new HashMap<>();

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

            // Search field and Select All checkbox
            HBox searchBox = new HBox(10);
            searchBox.setAlignment(Pos.CENTER_LEFT);

            selectAllCheckBox = new CheckBox("Select All");
            selectAllCheckBox.setStyle("-fx-font-size: 13px; -fx-text-fill: #192a51;");
            selectAllCheckBox.setOnAction(e -> {
                boolean selectAll = selectAllCheckBox.isSelected();
                table.getSelectionModel().clearSelection();

                if (selectAll) {
                    for (int i = 0; i < table.getItems().size(); i++) {
                        table.getSelectionModel().select(i);
                    }
                }
                updateAnalyzeSelectedButton();
            });

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

            searchBox.getChildren().addAll(selectAllCheckBox, searchField);

            VBox.setMargin(searchBox, new Insets(0, 0, 20, 0));

            // Buttons
            Button loadBtn = new Button("📁 Load Files");
            loadBtn.setStyle("-fx-background-color: #d5c6e0; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            loadBtn.setMinWidth(130);

            startBtn = new Button("▶ Start Analysis");
            startBtn.setStyle("-fx-background-color: #aaa1c8; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            startBtn.setMinWidth(130);

            analyzeSelectedBtn = new Button("✅ Analyze Selected (0)");
            analyzeSelectedBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            analyzeSelectedBtn.setMinWidth(150);
            analyzeSelectedBtn.setDisable(true);

            analyzeAllBtn = new Button("🔀 Analyze All");
            analyzeAllBtn.setStyle("-fx-background-color: #192a51; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            analyzeAllBtn.setMinWidth(130);

            Button deleteBtn = new Button("🗑 Delete");
            deleteBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            deleteBtn.setMinWidth(130);

            cancelBtn = new Button("✕ Cancel");
            cancelBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
            cancelBtn.setMinWidth(130);
            cancelBtn.setDisable(true);

            // Hover effects
            loadBtn.setOnMouseEntered(e -> loadBtn.setStyle("-fx-background-color: #c0a8d0; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"));
            loadBtn.setOnMouseExited(e -> loadBtn.setStyle("-fx-background-color: #d5c6e0; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;"));

            startBtn.setOnMouseEntered(e -> startBtn.setStyle("-fx-background-color: #8f84b3; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"));
            startBtn.setOnMouseExited(e -> startBtn.setStyle("-fx-background-color: #aaa1c8; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;"));

            analyzeSelectedBtn.setOnMouseEntered(e -> {
                if (!analyzeSelectedBtn.isDisable()) {
                    analyzeSelectedBtn.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);");
                }
            });
            analyzeSelectedBtn.setOnMouseExited(e -> {
                if (!analyzeSelectedBtn.isDisable()) {
                    analyzeSelectedBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;");
                }
            });

            analyzeAllBtn.setOnMouseEntered(e -> analyzeAllBtn.setStyle("-fx-background-color: #2a3a71; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"));
            analyzeAllBtn.setOnMouseExited(e -> analyzeAllBtn.setStyle("-fx-background-color: #192a51; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;"));

            deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-background-color: #6f547d; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3);"));
            deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25 12 25; -fx-background-radius: 8; -fx-border-radius: 8;"));

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

            cancelBtn.setOnAction(e -> handleCancelAction());

            // Button container
            HBox buttonContainer = new HBox(10);
            buttonContainer.setAlignment(Pos.CENTER);
            buttonContainer.setPadding(new Insets(0, 0, 20, 0));
            buttonContainer.getChildren().addAll(loadBtn, startBtn, analyzeSelectedBtn, analyzeAllBtn, deleteBtn, cancelBtn);

            // Overall progress indicator for multi-file analysis
            overallProgressIndicator = new ProgressIndicator(0);
            overallProgressIndicator.setPrefSize(24, 24);

            overallProgressLabel = new Label();
            overallProgressLabel.setStyle("-fx-text-fill: #192a51; -fx-font-size: 12px; -fx-font-weight: bold;");

            overallProgressBox = new HBox(10, overallProgressIndicator, overallProgressLabel);
            overallProgressBox.setAlignment(Pos.CENTER);
            overallProgressBox.setPadding(new Insets(10));
            overallProgressBox.setVisible(false);
            overallProgressBox.setManaged(false);

            // Individual progress bar with animation
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

            // TableView with MULTIPLE selection
            table = new TableView<>();
            table.setStyle("-fx-control-inner-background: white; -fx-background-color: #f5e6e8; -fx-border-color: #d5c6e0; -fx-border-radius: 8;");
            table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            masterData = FXCollections.observableArrayList();

            table.setPrefHeight(300);
            table.setMinHeight(200);
            VBox.setVgrow(table, Priority.ALWAYS);
            VBox.setMargin(table, new Insets(0, 0, 20, 0));
            table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

            // Listen to selection changes
            table.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                updateAnalyzeSelectedButton();
            });

            // Columns - Checkbox column first
            TableColumn<FileInfo, Boolean> selectCol = new TableColumn<>("");
            selectCol.setMinWidth(40);
            selectCol.setMaxWidth(40);
            selectCol.setCellValueFactory(param -> new SimpleBooleanProperty(false));
            selectCol.setCellFactory(column -> new TableCell<FileInfo, Boolean>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    checkBox.setOnAction(e -> {
                        FileInfo fileInfo = getTableView().getItems().get(getIndex());
                        if (checkBox.isSelected()) {
                            table.getSelectionModel().select(fileInfo);
                        } else {
                            table.getSelectionModel().clearSelection(getIndex());
                        }
                        updateAnalyzeSelectedButton();
                    });
                }

                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        FileInfo fileInfo = getTableView().getItems().get(getIndex());
                        checkBox.setSelected(table.getSelectionModel().isSelected(getIndex()));
                        setGraphic(checkBox);
                    }
                }
            });

            TableColumn<FileInfo, String> nameCol = new TableColumn<>("Name");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
            nameCol.setMinWidth(120);

            TableColumn<FileInfo, String> sizeCol = new TableColumn<>("Size");
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
                            case "cancelled":
                                setStyle("-fx-text-fill: #9E9E9E; -fx-font-weight: bold; -fx-background-color: #F5F5F5; -fx-padding: 2 6 2 6; -fx-background-radius: 8;");
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

            // Results column with View button
            TableColumn<FileInfo, Void> actionCol = new TableColumn<>("Results");
            actionCol.setMinWidth(80);
            actionCol.setCellFactory(column -> new TableCell<FileInfo, Void>() {
                private final Button viewBtn = new Button("View");

                {
                    viewBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 12 4 12; -fx-background-radius: 4;");
                    viewBtn.setOnMouseEntered(e -> viewBtn.setStyle("-fx-background-color: #45a049; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 12 4 12; -fx-background-radius: 4;"));
                    viewBtn.setOnMouseExited(e -> viewBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 4 12 4 12; -fx-background-radius: 4;"));

                    viewBtn.setOnAction(e -> {
                        FileInfo fileInfo = getTableView().getItems().get(getIndex());
                        if ("Completed".equals(fileInfo.getStatus())) {
                            Map<String, Object> results = allResults.get(fileInfo.getName());
                            if (results != null) {
                                showResultsInDialog(results, fileInfo.getName());
                            }
                        }
                    });
                }

                @Override
                protected void updateItem(Void item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        FileInfo fileInfo = getTableView().getItems().get(getIndex());
                        if ("Completed".equals(fileInfo.getStatus())) {
                            setGraphic(viewBtn);
                        } else {
                            setGraphic(null);
                        }
                    }
                }
            });

            table.getColumns().addAll(selectCol, nameCol, sizeCol, dateCol, statusCol, pathCol, actionCol);

            // Results container
            resultsContainer = new VBox();
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
                    overallProgressBox,
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

                            synchronized (lock) {
                                masterData.add(fileInfo);
                            }
                        } catch (Exception ex) {
                            System.err.println("Error loading file: " + file.getName());
                        }
                    }

                    table.refresh();
                    showStatusMessage("Loaded " + files.size() + " file(s) successfully", 2000);

                    // تحديث زر Analyze Selected
                    updateAnalyzeSelectedButton();

                    if (!masterData.isEmpty()) {
                        table.getSelectionModel().select(0);
                    }
                }
            });

            // Delete Selected
            deleteBtn.setOnAction(e -> {
                ObservableList<FileInfo> selected = table.getSelectionModel().getSelectedItems();
                if (!selected.isEmpty()) {
                    synchronized (lock) {
                        masterData.removeAll(selected);
                    }

                    // Remove from results
                    for (FileInfo fileInfo : selected) {
                        allResults.remove(fileInfo.getName());
                    }

                    showStatusMessage("Removed " + selected.size() + " file(s)", 1500);

                    // تحديث زر Analyze Selected
                    updateAnalyzeSelectedButton();
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "No file selected!");
                    alert.showAndWait();
                }
            });

            // Start Analysis (Single File)
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

                    // Set mode
                    isMultiAnalysisMode = false;

                    // Hide previous results
                    resultsContainer.setVisible(false);

                    // Disable/enable buttons
                    startBtn.setDisable(true);
                    analyzeSelectedBtn.setDisable(true);
                    analyzeAllBtn.setDisable(true);
                    cancelBtn.setDisable(false);

                    // Disable selection during analysis
                    selectAllCheckBox.setDisable(true);
                    table.setDisable(true);

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
                                // Store results
                                allResults.put(selected.getName(), results);
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

            // Analyze Selected Files
            analyzeSelectedBtn.setOnAction(e -> {
                ObservableList<FileInfo> selectedFiles = table.getSelectionModel().getSelectedItems();

                if (selectedFiles.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Please select files to analyze!");
                    alert.showAndWait();
                    return;
                }

                // تحويل إلى قائمة
                List<FileInfo> filesToAnalyze = new ArrayList<>(selectedFiles);

                // تأكيد من المستخدم
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Confirm Analysis");
                confirm.setHeaderText("You are about to analyze " + filesToAnalyze.size() + " selected files");
                confirm.setContentText("Do you want to continue?");

                Optional<ButtonType> result = confirm.showAndWait();
                if (!result.isPresent() || result.get() != ButtonType.OK) {
                    return;
                }

                // إخفاء النتائج السابقة
                resultsContainer.setVisible(false);

                // تعطيل الأزرار
                startBtn.setDisable(true);
                analyzeSelectedBtn.setDisable(true);
                analyzeAllBtn.setDisable(true);
                cancelBtn.setDisable(false);

                // تعطيل التحديد أثناء التحليل
                selectAllCheckBox.setDisable(true);
                table.setDisable(true);

                // تفعيل وضع التحليل المتعدد
                isMultiAnalysisMode = true;

                // إظهار مؤشر التقدم العام
                overallProgressBox.setVisible(true);
                overallProgressBox.setManaged(true);
                overallProgressIndicator.setProgress(0);
                overallProgressLabel.setText("Preparing to analyze " + filesToAnalyze.size() + " selected files...");

                // إخفاء مؤشر التقدم المفرد
                progressBox.setVisible(false);

                // مسح النتائج السابقة للملفات المحددة فقط
                for (FileInfo fileInfo : filesToAnalyze) {
                    allResults.remove(fileInfo.getName());
                }

                // تحديث حالة الملفات المحددة
                for (FileInfo fileInfo : filesToAnalyze) {
                    fileInfo.setStatus("Analyzing");
                }
                table.refresh();

                // إنشاء listener لتحديث التقدم
                MultiThreadedAnalysisManager.AnalysisProgressListener listener =
                        new MultiThreadedAnalysisManager.AnalysisProgressListener() {
                            @Override
                            public void onProgressUpdate(double overallProgress, int completedFiles, int totalFiles) {
                                Platform.runLater(() -> {
                                    overallProgressIndicator.setProgress(overallProgress);
                                    overallProgressLabel.setText(
                                            String.format("Processing: %d/%d selected files (%.0f%%)",
                                                    completedFiles, totalFiles, overallProgress * 100)
                                    );
                                });
                            }

                            @Override
                            public void onFileComplete(String fileName, Map<String, Object> results) {
                                Platform.runLater(() -> {
                                    // تخزين النتائج
                                    allResults.put(fileName, results);

                                    // تحديث الجدول
                                    for (FileInfo fileInfo : masterData) {
                                        if (fileInfo.getName().equals(fileName)) {
                                            fileInfo.setStatus("Completed");
                                            break;
                                        }
                                    }
                                    table.refresh();
                                });
                            }

                            @Override
                            public void onFileError(String fileName, String errorMessage) {
                                Platform.runLater(() -> {
                                    // تحديث حالة الملف إلى خطأ
                                    for (FileInfo fileInfo : masterData) {
                                        if (fileInfo.getName().equals(fileName)) {
                                            fileInfo.setStatus("Error");
                                            break;
                                        }
                                    }
                                    table.refresh();
                                });
                            }

                            @Override
                            public void onAnalysisComplete(Map<String, AnalysisResult> analysisResults) {
                                Platform.runLater(() -> {
                                    resetUIAfterAnalysis();
                                    overallProgressBox.setVisible(false);
                                    overallProgressBox.setManaged(false);

                                    // تمكين التحديد مجدداً
                                    selectAllCheckBox.setDisable(false);
                                    table.setDisable(false);

                                    // إظهار ملخص النتائج
                                    showSelectedAnalysisSummary(filesToAnalyze.size());
                                });
                            }
                        };

                // بدء التحليل المتعدد للملفات المحددة فقط
                multiAnalysisManager.analyzeFiles(filesToAnalyze, listener);
            });

            // Analyze All Files
            analyzeAllBtn.setOnAction(e -> {
                List<FileInfo> filesToAnalyze;
                synchronized (lock) {
                    if (masterData.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING, "No files loaded to analyze!");
                        alert.showAndWait();
                        return;
                    }
                    filesToAnalyze = new ArrayList<>(masterData);
                }

                // Confirm for large number of files
                if (filesToAnalyze.size() > 10) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Confirm Bulk Analysis");
                    confirm.setHeaderText("You are about to analyze " + filesToAnalyze.size() + " files");
                    confirm.setContentText("This may take some time. Do you want to continue?");

                    Optional<ButtonType> result = confirm.showAndWait();
                    if (!result.isPresent() || result.get() != ButtonType.OK) {
                        return;
                    }
                }

                // Hide previous results
                resultsContainer.setVisible(false);

                // Disable buttons
                startBtn.setDisable(true);
                analyzeSelectedBtn.setDisable(true);
                analyzeAllBtn.setDisable(true);
                cancelBtn.setDisable(false);

                // Disable selection during analysis
                selectAllCheckBox.setDisable(true);
                table.setDisable(true);

                // Set multi-analysis mode
                isMultiAnalysisMode = true;

                // Show overall progress
                overallProgressBox.setVisible(true);
                overallProgressBox.setManaged(true);
                overallProgressIndicator.setProgress(0);
                overallProgressLabel.setText("Preparing to analyze " + filesToAnalyze.size() + " files...");

                // Hide individual progress
                progressBox.setVisible(false);

                // Clear previous results for files being analyzed
                for (FileInfo fileInfo : filesToAnalyze) {
                    allResults.remove(fileInfo.getName());
                }

                // Update all file statuses
                for (FileInfo fileInfo : filesToAnalyze) {
                    fileInfo.setStatus("Analyzing");
                }
                table.refresh();

                // Create listener for progress updates
                MultiThreadedAnalysisManager.AnalysisProgressListener listener =
                        new MultiThreadedAnalysisManager.AnalysisProgressListener() {
                            @Override
                            public void onProgressUpdate(double overallProgress, int completedFiles, int totalFiles) {
                                Platform.runLater(() -> {
                                    overallProgressIndicator.setProgress(overallProgress);
                                    overallProgressLabel.setText(
                                            String.format("Processing: %d/%d files (%.0f%%)",
                                                    completedFiles, totalFiles, overallProgress * 100)
                                    );
                                });
                            }

                            @Override
                            public void onFileComplete(String fileName, Map<String, Object> results) {
                                Platform.runLater(() -> {
                                    // Store results
                                    allResults.put(fileName, results);

                                    // Update table status
                                    for (FileInfo fileInfo : masterData) {
                                        if (fileInfo.getName().equals(fileName)) {
                                            fileInfo.setStatus("Completed");
                                            break;
                                        }
                                    }
                                    table.refresh();
                                });
                            }

                            @Override
                            public void onFileError(String fileName, String errorMessage) {
                                Platform.runLater(() -> {
                                    // Update file status
                                    for (FileInfo fileInfo : masterData) {
                                        if (fileInfo.getName().equals(fileName)) {
                                            fileInfo.setStatus("Error");
                                            break;
                                        }
                                    }
                                    table.refresh();
                                });
                            }

                            @Override
                            public void onAnalysisComplete(Map<String, AnalysisResult> analysisResults) {
                                Platform.runLater(() -> {
                                    resetUIAfterAnalysis();
                                    overallProgressBox.setVisible(false);
                                    overallProgressBox.setManaged(false);

                                    // Enable selection again
                                    selectAllCheckBox.setDisable(false);
                                    table.setDisable(false);

                                    // Show summary
                                    showAnalysisSummary();
                                });
                            }
                        };

                // Start multi-threaded analysis
                multiAnalysisManager.analyzeFiles(filesToAnalyze, listener);
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

            // Double-click to view results
            table.setRowFactory(tv -> {
                TableRow<FileInfo> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        FileInfo fileInfo = row.getItem();
                        if ("Completed".equals(fileInfo.getStatus())) {
                            Map<String, Object> results = allResults.get(fileInfo.getName());
                            if (results != null) {
                                showResultsInDialog(results, fileInfo.getName());
                            }
                        }
                    }
                });
                return row;
            });

            // Create scene with responsive size
            Scene scene = new Scene(root, 1200, 750);

            String hoverCSS = """
                .table-row-cell:hover {
                    -fx-background-color: #f0e6f5;
                    -fx-cursor: hand;
                }
                
                .button:hover {
                    -fx-scale-x: 1.02;
                    -fx-scale-y: 1.02;
                }
                
                .button {
                    -fx-transition: all 0.2s ease;
                }
            """;

            scene.getStylesheets().add("data:text/css," + hoverCSS);

            primaryStage.setTitle("VioletLens - Advanced Text Analyzer (Multi-Threaded)");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
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
     * Update Analyze Selected button text
     */
    private void updateAnalyzeSelectedButton() {
        int selectedCount = table.getSelectionModel().getSelectedItems().size();
        analyzeSelectedBtn.setText("✅ Analyze Selected (" + selectedCount + ")");
        analyzeSelectedBtn.setDisable(selectedCount == 0);
    }

    /**
     * Display results in main view
     */
    private void displayResults(Map<String, Object> results, AnalysisResultView analysisResultView, VBox resultsContainer) {
        Platform.runLater(() -> {
            resultsContainer.setVisible(true);
            resultsContainer.setManaged(true);

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

    /**
     * Show results in dialog window
     */
    private void showResultsInDialog(Map<String, Object> results, String fileName) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Analysis Results - " + fileName);
        dialog.setHeaderText("Detailed analysis for " + fileName);

        AnalysisResultView resultView = new AnalysisResultView();
        resultView.displayResults(results);

        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);

        dialog.getDialogPane().setContent(resultView);
        dialog.getDialogPane().setMinSize(600, 500);

        dialog.showAndWait();
    }

    /**
     * Create analysis task for single file
     */
    private Task<Map<String, Object>> createAnalysisTask(File file, String fileName) {
        return new Task<Map<String, Object>>() {
            @Override
            protected Map<String, Object> call() throws Exception {
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
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Export");
            alert.setHeaderText("Export feature");
            alert.setContentText("Export as " + format + " would be implemented here.");
            alert.showAndWait();
        });
    }

    private String generateTextExport(Map<String, Object> results) {
        return "";
    }

    private String generateCsvExport(Map<String, Object> results) {
        return "";
    }

    private void copyResultsToClipboard(Map<String, Object> results) {
        Platform.runLater(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString("Analysis results copied to clipboard");
            clipboard.setContent(content);

            showStatusMessage("Results copied to clipboard!", 1500);
        });
    }

    private void handleCancelAction() {
        if (isMultiAnalysisMode && multiAnalysisManager != null) {
            // Cancel multi-threaded analysis
            multiAnalysisManager.cancelAll();
            statusLabel.setText("Cancelling all analyses...");
            overallProgressLabel.setText("Cancelling...");

            // Update file statuses
            Platform.runLater(() -> {
                for (FileInfo fileInfo : masterData) {
                    if ("Analyzing".equals(fileInfo.getStatus())) {
                        fileInfo.setStatus("Cancelled");
                    }
                }
                table.refresh();
                // Enable selection again
                selectAllCheckBox.setDisable(false);
                table.setDisable(false);
            });
        } else if (currentTask != null && currentTask.isRunning()) {
            // Cancel single file analysis
            currentTask.cancel(true);
            if (currentThread != null && currentThread.isAlive()) {
                currentThread.interrupt();
            }
            statusLabel.setText("Cancelling analysis...");
            // Enable selection again
            selectAllCheckBox.setDisable(false);
            table.setDisable(false);
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
            analyzeSelectedBtn.setDisable(false);
            analyzeAllBtn.setDisable(false);
            cancelBtn.setDisable(true);

            // Enable selection
            selectAllCheckBox.setDisable(false);
            table.setDisable(false);

            if (isMultiAnalysisMode) {
                isMultiAnalysisMode = false;
                overallProgressBox.setVisible(false);
                overallProgressBox.setManaged(false);
            } else {
                progressBar.progressProperty().unbind();
                progressBar.setVisible(false);
                progressBox.setVisible(false);
                stopProgressAnimation();
            }

            statusLabel.setText("Ready to analyze");
            currentTask = null;
            currentThread = null;

            // Update Analyze Selected button
            updateAnalyzeSelectedButton();
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

    private void showAnalysisSummary() {
        if (allResults.isEmpty()) {
            showStatusMessage("No files were analyzed successfully", 3000);
            return;
        }

        int successCount = allResults.size();
        int errorCount = 0;
        int cancelledCount = 0;

        for (FileInfo fileInfo : masterData) {
            if ("Error".equals(fileInfo.getStatus())) {
                errorCount++;
            } else if ("Cancelled".equals(fileInfo.getStatus())) {
                cancelledCount++;
            }
        }

        String summary = String.format(
                "Multi-threaded Analysis Complete!\n\n" +
                        "✓ Successful: %d files\n" +
                        "✗ Errors: %d files\n" +
                        "⏸ Cancelled: %d files\n\n" +
                        "Click 'View' button or double-click any completed file to see details.",
                successCount, errorCount, cancelledCount
        );

        Alert summaryAlert = new Alert(Alert.AlertType.INFORMATION);
        summaryAlert.setTitle("Bulk Analysis Complete");
        summaryAlert.setHeaderText("Multi-threaded Analysis Results");
        summaryAlert.setContentText(summary);
        summaryAlert.show();

        statusLabel.setText("Bulk analysis complete - " + successCount + " files analyzed");
    }

    private void showSelectedAnalysisSummary(int totalSelected) {
        if (allResults.isEmpty()) {
            showStatusMessage("No files were analyzed successfully", 3000);
            return;
        }

        int successCount = 0;
        int errorCount = 0;

        for (FileInfo fileInfo : table.getSelectionModel().getSelectedItems()) {
            if ("Completed".equals(fileInfo.getStatus())) {
                successCount++;
            } else if ("Error".equals(fileInfo.getStatus())) {
                errorCount++;
            }
        }

        String summary = String.format(
                "Selected Files Analysis Complete!\n\n" +
                        "✓ Successful: %d/%d files\n" +
                        "✗ Errors: %d/%d files\n\n" +
                        "Click 'View' button or double-click any completed file to see details.",
                successCount, totalSelected, errorCount, totalSelected
        );

        Alert summaryAlert = new Alert(Alert.AlertType.INFORMATION);
        summaryAlert.setTitle("Selected Files Analysis Complete");
        summaryAlert.setHeaderText("Multi-threaded Analysis Results");
        summaryAlert.setContentText(summary);
        summaryAlert.show();

        statusLabel.setText("Selected files analysis complete - " + successCount + "/" + totalSelected + " successful");
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

        Label taglineLabel = new Label("Multi-Threaded Text Analysis Tool");
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