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
    private AnalysisResultView analysisResultView;

    @Override
    public void start(Stage primaryStage) {
        try {
            analysisManager = new AnalysisManager();

            BorderPane root = new BorderPane();
            root.setStyle("-fx-background-color: #f5e6e8;");

            // Header
            HBox headerBox = createHeader();
            root.setTop(headerBox);

            // Main content
            VBox mainContent = new VBox(15);
            mainContent.setPadding(new Insets(20));
            mainContent.setStyle("-fx-background-color: #f5e6e8;");

            ScrollPane scrollPane = new ScrollPane(mainContent);
            scrollPane.setFitToWidth(true);
            root.setCenter(scrollPane);

            // App Description
            Label descriptionLabel = new Label("Analyze your text files with powerful insights");
            descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #192a51;");

            // Search field
            TextField searchField = new TextField();
            searchField.setPromptText("Search by file name...");
            searchField.setStyle("-fx-background-color: white; -fx-border-color: #d5c6e0; -fx-border-radius: 5;");

            // Buttons
            Button loadBtn = new Button("📁 Load Files");
            loadBtn.setStyle("-fx-background-color: #d5c6e0; -fx-text-fill: #192a51; -fx-padding: 8 15;");

            startBtn = new Button("▶ Start Analysis");
            startBtn.setStyle("-fx-background-color: #aaa1c8; -fx-text-fill: #192a51; -fx-padding: 8 15;");

            Button deleteBtn = new Button("🗑 Delete");
            deleteBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-padding: 8 15;");

            cancelBtn = new Button("✕ Cancel");
            cancelBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-padding: 8 15;");
            cancelBtn.setDisable(true);

            HBox buttonContainer = new HBox(10, loadBtn, startBtn, deleteBtn, cancelBtn);
            buttonContainer.setAlignment(Pos.CENTER);

            // Progress bar
            progressBar = new ProgressBar(0);
            progressBar.setPrefWidth(400);
            progressBar.setVisible(false);

            statusLabel = new Label("Ready to analyze");
            progressBox = new VBox(10, progressBar, statusLabel);
            progressBox.setAlignment(Pos.CENTER);
            progressBox.setVisible(false);

            // TableView
            table = new TableView<>();
            masterData = FXCollections.observableArrayList();
            table.setPrefHeight(250);

            TableColumn<FileInfo, String> nameCol = new TableColumn<>("Name");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

            TableColumn<FileInfo, Long> sizeCol = new TableColumn<>("Size (KB)");
            sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));

            TableColumn<FileInfo, String> statusCol = new TableColumn<>("Status");
            statusCol.setCellValueFactory(param -> param.getValue().statusProperty());

            TableColumn<FileInfo, String> pathCol = new TableColumn<>("Path");
            pathCol.setCellValueFactory(new PropertyValueFactory<>("path"));

            table.getColumns().addAll(nameCol, sizeCol, statusCol, pathCol);

            // Results container
            resultsContainer = new VBox();
            resultsContainer.setVisible(false);

            analysisResultView = new AnalysisResultView();
            resultsContainer.getChildren().add(analysisResultView);

            // Add components
            mainContent.getChildren().addAll(
                    descriptionLabel,
                    searchField,
                    buttonContainer,
                    progressBox,
                    table,
                    resultsContainer
            );

            // FileChooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Text Files");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.md", "*.java")
            );

            // Load Files
            loadBtn.setOnAction(e -> {
                List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);
                if (files != null && !files.isEmpty()) {
                    for (File file : files) {
                        try {
                            String fileName = file.getName();
                            long fileSize = file.length() / 1024;
                            String lastModified = "N/A";
                            String filePath = file.getAbsolutePath();

                            FileInfo fileInfo = new FileInfo(fileName, fileSize, lastModified, filePath);
                            masterData.add(fileInfo);
                        } catch (Exception ex) {
                            showAlert("Error", "Failed to load file: " + file.getName());
                        }
                    }
                    showStatusMessage("Loaded " + files.size() + " file(s)", 2000);
                }
            });

            // Delete Selected
            deleteBtn.setOnAction(e -> {
                ObservableList<FileInfo> selected = table.getSelectionModel().getSelectedItems();
                if (!selected.isEmpty()) {
                    masterData.removeAll(selected);
                    showStatusMessage("Files deleted", 1500);
                } else {
                    showAlert("Warning", "Please select files to delete");
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

                    // Show progress bar
                    progressBox.setVisible(true);
                    progressBar.setVisible(true);
                    progressBar.setProgress(0);
                    startBtn.setDisable(true);
                    cancelBtn.setDisable(false);
                    statusLabel.setText("Starting analysis...");

                    // Create analysis task
                    currentTask = analysisManager.createAnalysisTask(file);

                    // Bind progress bar
                    progressBar.progressProperty().bind(currentTask.progressProperty());

                    // Bind status messages
                    currentTask.messageProperty().addListener((obs, oldMsg, newMsg) -> {
                        Platform.runLater(() -> statusLabel.setText(newMsg));
                    });

                    // On success
                    currentTask.setOnSucceeded(event -> {
                        Platform.runLater(() -> {
                            Map<String, Object> results = currentTask.getValue();
                            if (results != null) {
                                displayResults(results);
                                selected.setStatus("Completed");
                                table.refresh();
                            }
                            resetUIAfterAnalysis();
                        });
                    });

                    // On failure
                    currentTask.setOnFailed(event -> {
                        Platform.runLater(() -> {
                            selected.setStatus("Error");
                            table.refresh();
                            showAlert("Analysis Error", currentTask.getException().getMessage());
                            resetUIAfterAnalysis();
                        });
                    });

                    // On cancellation
                    currentTask.setOnCancelled(event -> {
                        Platform.runLater(() -> {
                            selected.setStatus("Cancelled");
                            table.refresh();
                            statusLabel.setText("Analysis cancelled");
                            resetUIAfterAnalysis();
                        });
                    });

                    // Cancel button action
                    cancelBtn.setOnAction(cancelEvent -> {
                        if (currentTask != null && currentTask.isRunning()) {
                            currentTask.cancel();
                        }
                    });

                    // Start task
                    currentThread = new Thread(currentTask);
                    currentThread.setDaemon(true);
                    currentThread.start();

                } else {
                    showAlert("Warning", "Please select a file to analyze");
                }
            });

            // Search functionality
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

            // Scene
            Scene scene = new Scene(root, 900, 650);

            // Load CSS if exists
            try {
                scene.getStylesheets().add(getClass().getResource("/CSS/Style.css").toExternalForm());
            } catch (Exception e) {
                // Continue without external CSS
            }

            primaryStage.setTitle("Multi-Threaded Text Analyzer");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            primaryStage.show();

        } catch (Exception e) {
            showAlert("Error", "Failed to start application: " + e.getMessage());
        }
    }

    private void displayResults(Map<String, Object> results) {
        resultsContainer.setVisible(true);
        analysisResultView.displayResults(results);

        // Bind result buttons
        Button closeBtn = analysisResultView.getCloseButton();
        closeBtn.setOnAction(e -> {
            resultsContainer.setVisible(false);
        });

        Button exportTxtBtn = analysisResultView.getExportTxtButton();
        exportTxtBtn.setOnAction(e -> exportResults(results, "txt"));

        Button exportCsvBtn = analysisResultView.getExportCsvButton();
        exportCsvBtn.setOnAction(e -> exportResults(results, "csv"));

        Button copyBtn = analysisResultView.getCopyToClipboardButton();
        copyBtn.setOnAction(e -> copyResultsToClipboard(results));
    }

    private void exportResults(Map<String, Object> results, String format) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Analysis Results");

        if (format.equals("txt")) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt")
            );
            fileChooser.setInitialFileName("analysis_results_" + results.get("fileName") + ".txt");
        } else if (format.equals("csv")) {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );
            fileChooser.setInitialFileName("analysis_results_" + results.get("fileName") + ".csv");
        }

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                String content = "";
                if (format.equals("txt")) {
                    content = generateTextExport(results);
                } else if (format.equals("csv")) {
                    content = generateCsvExport(results);
                }

                java.nio.file.Files.write(file.toPath(), content.getBytes());
                showAlert("Success", "Results saved to: " + file.getAbsolutePath());

            } catch (Exception e) {
                showAlert("Error", "Failed to save file: " + e.getMessage());
            }
        }
    }

    private String generateTextExport(Map<String, Object> results) {
        StringBuilder content = new StringBuilder();
        content.append("Text Analysis Results\n");
        content.append("=====================\n\n");
        content.append("File Name: ").append(results.get("fileName")).append("\n");
        content.append("Total Words: ").append(results.get("totalWords")).append("\n");
        content.append("Unique Words: ").append(results.get("uniqueWords")).append("\n");
        content.append("Characters (with spaces): ").append(results.get("charsWithSpaces")).append("\n");
        content.append("Sentence Count: ").append(results.get("sentenceCount")).append("\n");
        content.append("Estimated Reading Time: ").append(results.get("readingTime")).append(" minutes\n\n");
        content.append("Most Frequent Words:\n");
        content.append(results.get("mostFrequent")).append("\n");

        return content.toString();
    }

    private String generateCsvExport(Map<String, Object> results) {
        StringBuilder content = new StringBuilder();
        content.append("Metric,Value\n");
        content.append("File Name,").append(results.get("fileName")).append("\n");
        content.append("Total Words,").append(results.get("totalWords")).append("\n");
        content.append("Unique Words,").append(results.get("uniqueWords")).append("\n");
        content.append("Characters (with spaces),").append(results.get("charsWithSpaces")).append("\n");
        content.append("Sentence Count,").append(results.get("sentenceCount")).append("\n");
        content.append("Reading Time,").append(results.get("readingTime")).append(" minutes\n");

        return content.toString();
    }

    private void copyResultsToClipboard(Map<String, Object> results) {
        try {
            String textContent = generateTextExport(results);
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(textContent);
            clipboard.setContent(content);

            showStatusMessage("Results copied to clipboard", 1500);
        } catch (Exception e) {
            showAlert("Error", "Failed to copy to clipboard: " + e.getMessage());
        }
    }

    private void resetUIAfterAnalysis() {
        Platform.runLater(() -> {
            startBtn.setDisable(false);
            cancelBtn.setDisable(true);
            progressBox.setVisible(false);
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            currentTask = null;
            currentThread = null;
        });
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
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

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> {
                        progressBox.setVisible(false);
                        statusLabel.setText("Ready to analyze");
                    });
                }
            }, duration);
        });
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setPadding(new Insets(10, 20, 10, 20));
        header.setStyle("-fx-background-color: #f5e6e8; -fx-border-color: #d5c6e0; -fx-border-width: 0 0 1 0;");

        Label title = new Label("📊 Multi-Threaded Text Analyzer");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #192a51;");

        header.getChildren().add(title);
        return header;
    }

    public static void main(String[] args) {
        launch(args);
    }
}