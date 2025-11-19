package com.example.textanalysisapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TextAnalyzerApp extends Application {
    private List<File> selectedFiles = new ArrayList<>();
    private VBox fileListContainer;
    private ProgressIndicator globalProgress;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("TextScope - Multi-Threaded Text Analysis");

        // Create main layout
        BorderPane mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("main-layout");

        // Create header
        mainLayout.setTop(createHeader());

        // Create center content
        mainLayout.setCenter(createCenterContent());

        // Create footer
        mainLayout.setBottom(createFooter());

        Scene scene = new Scene(mainLayout, 1000, 700);

        // For modular projects, use getResource with proper path
        scene.getStylesheets().add(getClass().getResource("/com/example/textanalysisapp/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header");
        header.setPadding(new Insets(20));
        header.setAlignment(Pos.CENTER_LEFT);

        // App title and logo
        HBox titleBox = new HBox(15);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("TextAnalyzer Pro");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Multi-Threaded Text Analysis");
        subtitle.getStyleClass().add("app-subtitle");

        VBox titleGroup = new VBox(5, title, subtitle);
        titleBox.getChildren().addAll(createLogo(), titleGroup);

        header.getChildren().add(titleBox);

        return header;
    }

    private StackPane createLogo() {
        StackPane logo = new StackPane();
        logo.getStyleClass().add("logo");
        Label logoText = new Label("TA");
        logoText.getStyleClass().add("logo-text");
        logo.getChildren().add(logoText);
        return logo;
    }

    private VBox createCenterContent() {
        VBox centerContent = new VBox(20);
        centerContent.getStyleClass().add("center-content");
        centerContent.setPadding(new Insets(25));

        // File selection section
        VBox fileSection = createFileSection();

        // Analysis controls
        HBox controlsSection = createControlsSection();

        // File list display
        VBox fileListSection = createFileListSection();

        // Progress section
        VBox progressSection = createProgressSection();

        centerContent.getChildren().addAll(fileSection, controlsSection, fileListSection, progressSection);

        return centerContent;
    }

    private VBox createFileSection() {
        VBox fileSection = new VBox(15);
        fileSection.getStyleClass().add("file-section");

        Label sectionTitle = new Label("Select Text Files for Analysis");
        sectionTitle.getStyleClass().add("section-title");

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button browseBtn = new Button("Browse Files");
        browseBtn.getStyleClass().addAll("btn", "btn-primary");
        browseBtn.setOnAction(e -> browseFiles());

        Button clearBtn = new Button("Clear Selection");
        clearBtn.getStyleClass().addAll("btn", "btn-secondary");
        clearBtn.setOnAction(e -> clearSelection());

        buttonBox.getChildren().addAll(browseBtn, clearBtn);

        fileSection.getChildren().addAll(sectionTitle, buttonBox);

        return fileSection;
    }

    private HBox createControlsSection() {
        HBox controlsSection = new HBox(15);
        controlsSection.setAlignment(Pos.CENTER_LEFT);

        Button analyzeBtn = new Button("Start Analysis");
        analyzeBtn.getStyleClass().addAll("btn", "btn-success");
        analyzeBtn.setDisable(true);
        analyzeBtn.setOnAction(e -> startAnalysis());

        Button stopBtn = new Button("Stop Analysis");
        stopBtn.getStyleClass().addAll("btn", "btn-danger");
        stopBtn.setDisable(true);

        CheckBox parallelProcessing = new CheckBox("Enable Parallel Processing");
        parallelProcessing.setSelected(true);
        parallelProcessing.getStyleClass().add("checkbox");

        controlsSection.getChildren().addAll(analyzeBtn, stopBtn, parallelProcessing);

        return controlsSection;
    }

    private VBox createFileListSection() {
        VBox fileListSection = new VBox(10);
        fileListSection.getStyleClass().add("file-list-section");

        Label sectionTitle = new Label("Selected Files");
        sectionTitle.getStyleClass().add("section-subtitle");

        fileListContainer = new VBox(8);
        fileListContainer.getStyleClass().add("file-list-container");

        ScrollPane scrollPane = new ScrollPane(fileListContainer);
        scrollPane.getStyleClass().add("file-scroll-pane");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(200);

        fileListSection.getChildren().addAll(sectionTitle, scrollPane);

        return fileListSection;
    }

    private VBox createProgressSection() {
        VBox progressSection = new VBox(15);
        progressSection.getStyleClass().add("progress-section");

        Label sectionTitle = new Label("Analysis Progress");
        sectionTitle.getStyleClass().add("section-subtitle");

        globalProgress = new ProgressIndicator(0);
        globalProgress.getStyleClass().add("global-progress");
        globalProgress.setVisible(false);

        statusLabel = new Label("Ready to analyze files");
        statusLabel.getStyleClass().add("status-label");

        progressSection.getChildren().addAll(sectionTitle, globalProgress, statusLabel);

        return progressSection;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.getStyleClass().add("footer");
        footer.setPadding(new Insets(15));
        footer.setAlignment(Pos.CENTER);

        Label footerText = new Label("TextAnalyzer Pro - Concurrency Programming Project © 2025-2026");
        footerText.getStyleClass().add("footer-text");

        footer.getChildren().add(footerText);

        return footer;
    }

    private void browseFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Text Files");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(null);
        if (files != null) {
            selectedFiles.addAll(files);
            updateFileList();
            updateAnalysisButton();
        }
    }

    private void clearSelection() {
        selectedFiles.clear();
        updateFileList();
        updateAnalysisButton();
        statusLabel.setText("Selection cleared");
    }

    private void updateFileList() {
        fileListContainer.getChildren().clear();

        for (File file : selectedFiles) {
            HBox fileItem = createFileItem(file);
            fileListContainer.getChildren().add(fileItem);
        }

        if (selectedFiles.isEmpty()) {
            Label emptyLabel = new Label("No files selected. Click 'Browse Files' to add files.");
            emptyLabel.getStyleClass().add("empty-label");
            fileListContainer.getChildren().add(emptyLabel);
        }
    }

    private HBox createFileItem(File file) {
        HBox fileItem = new HBox(10);
        fileItem.getStyleClass().add("file-item");
        fileItem.setAlignment(Pos.CENTER_LEFT);
        fileItem.setPadding(new Insets(8));

        // File icon
        StackPane fileIcon = new StackPane();
        fileIcon.getStyleClass().add("file-icon");
        Label fileType = new Label("TXT");
        fileType.getStyleClass().add("file-type");
        fileIcon.getChildren().add(fileType);

        // File info
        VBox fileInfo = new VBox(3);
        Label fileName = new Label(file.getName());
        fileName.getStyleClass().add("file-name");

        Label filePath = new Label(file.getParent());
        filePath.getStyleClass().add("file-path");

        fileInfo.getChildren().addAll(fileName, filePath);

        // Remove button
        Button removeBtn = new Button("×");
        removeBtn.getStyleClass().add("remove-btn");
        removeBtn.setOnAction(e -> removeFile(file));

        fileItem.getChildren().addAll(fileIcon, fileInfo, removeBtn);

        return fileItem;
    }

    private void removeFile(File file) {
        selectedFiles.remove(file);
        updateFileList();
        updateAnalysisButton();
    }

    private void updateAnalysisButton() {
        if (selectedFiles.isEmpty()) {
            statusLabel.setText("Ready to analyze files");
        } else {
            statusLabel.setText(selectedFiles.size() + " file(s) selected for analysis");
        }
    }

    private void startAnalysis() {
        // Placeholder for analysis logic
        globalProgress.setVisible(true);
        statusLabel.setText("Analysis in progress...");

        // Simulate analysis completion
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    globalProgress.setVisible(false);
                    statusLabel.setText("Analysis completed!");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}