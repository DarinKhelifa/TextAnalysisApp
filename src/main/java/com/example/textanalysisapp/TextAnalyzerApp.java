package com.example.textanalysisapp;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
    private BorderPane mainLayout;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("TextScope - Advanced Text Analysis Platform");

        // Create main layout with sidebar
        mainLayout = new BorderPane();
        mainLayout.getStyleClass().add("main-layout");

        // Create sidebar menu
        mainLayout.setLeft(createSidebarMenu());

        // Create header
        mainLayout.setTop(createHeader());

        // Create center content
        mainLayout.setCenter(createCenterContent());

        // Create footer
        mainLayout.setBottom(createFooter());

        Scene scene = new Scene(mainLayout, 1200, 800); // Increased width for sidebar
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    private VBox createSidebarMenu() {
        VBox sidebar = new VBox(20);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(220);
        sidebar.setPadding(new Insets(25, 15, 25, 15));

        // Sidebar header
        Label sidebarTitle = new Label("NAVIGATION");
        sidebarTitle.getStyleClass().add("sidebar-title");

        // Menu items
        VBox menuItems = new VBox(8);
        menuItems.getStyleClass().add("menu-items");

        // Create menu buttons
        Button dashboardBtn = createMenuButton("📊 Dashboard", true);
        Button fileAnalysisBtn = createMenuButton("📁 File Analysis", false);
        Button realTimeBtn = createMenuButton("⏱️ Real-time Analysis", false);
        Button historyBtn = createMenuButton("📋 Analysis History", false);
        Button settingsBtn = createMenuButton("⚙️ Settings", false);
        Button helpBtn = createMenuButton("❓ Help & Guide", false);

        menuItems.getChildren().addAll(
                dashboardBtn, fileAnalysisBtn, realTimeBtn,
                historyBtn, settingsBtn, helpBtn
        );

        // Add some statistics section
        VBox statsSection = createStatsSection();

        sidebar.getChildren().addAll(
                createSidebarLogo(),
                sidebarTitle,
                new Separator(),
                menuItems,
                new Separator(),
                statsSection
        );

        return sidebar;
    }

    private Button createMenuButton(String text, boolean active) {
        Button button = new Button(text);
        button.getStyleClass().add("menu-btn");
        if (active) {
            button.getStyleClass().add("menu-btn-active");
        }
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);

        button.setOnAction(e -> {
            // Remove active class from all buttons
            for (var child : ((VBox) button.getParent()).getChildren()) {
                if (child instanceof Button) {
                    child.getStyleClass().remove("menu-btn-active");
                }
            }
            // Add active class to clicked button
            button.getStyleClass().add("menu-btn-active");
            statusLabel.setText("Navigating to: " + text);
        });

        return button;
    }

    private HBox createSidebarLogo() {
        HBox logoContainer = new HBox(10);
        logoContainer.setAlignment(Pos.CENTER_LEFT);
        logoContainer.setPadding(new Insets(0, 0, 10, 0));

        // Create a modern logo using shapes
        StackPane logo = new StackPane();
        logo.getStyleClass().add("sidebar-logo");

        // Create a document icon with magnifying glass
        Rectangle docBase = new Rectangle(30, 36);
        docBase.setFill(Color.web("#3498db"));
        docBase.setArcWidth(5);
        docBase.setArcHeight(5);

        Rectangle docFold = new Rectangle(8, 8);
        docFold.setFill(Color.web("#2980b9"));
        docFold.setTranslateX(-11);
        docFold.setTranslateY(-14);

        Circle magnifier = new Circle(8, Color.web("#e74c3c"));
        magnifier.setTranslateX(8);
        magnifier.setTranslateY(8);

        Circle magnifierHandle = new Circle(4, Color.web("#c0392b"));
        magnifierHandle.setTranslateX(15);
        magnifierHandle.setTranslateY(15);

        logo.getChildren().addAll(docBase, docFold, magnifier, magnifierHandle);

        VBox logoText = new VBox(2);
        Label appName = new Label("TextScope");
        appName.getStyleClass().add("sidebar-app-name");
        Label appTagline = new Label("Analyze Smarter");
        appTagline.getStyleClass().add("sidebar-app-tagline");

        logoText.getChildren().addAll(appName, appTagline);

        logoContainer.getChildren().addAll(logo, logoText);
        return logoContainer;
    }

    private VBox createStatsSection() {
        VBox statsSection = new VBox(10);
        statsSection.getStyleClass().add("stats-section");

        Label statsTitle = new Label("QUICK STATS");
        statsTitle.getStyleClass().add("stats-title");

        VBox statsItems = new VBox(8);

        // Stat items
        HBox filesStat = createStatItem("Files Analyzed", "0");
        HBox wordsStat = createStatItem("Total Words", "0");
        HBox timeStat = createStatItem("Time Saved", "0h 0m");

        statsItems.getChildren().addAll(filesStat, wordsStat, timeStat);

        statsSection.getChildren().addAll(statsTitle, statsItems);
        return statsSection;
    }

    private HBox createStatItem(String label, String value) {
        HBox statItem = new HBox();
        statItem.setAlignment(Pos.CENTER_LEFT);
        statItem.setSpacing(5);

        Label statLabel = new Label(label);
        statLabel.getStyleClass().add("stat-label");

        Label statValue = new Label(value);
        statValue.getStyleClass().add("stat-value");

        // Add a small indicator dot
        Circle dot = new Circle(3);
        dot.setFill(Color.web("#27ae60"));

        statItem.getChildren().addAll(dot, statLabel, new Region(), statValue);
        HBox.setHgrow(statItem.getChildren().get(2), Priority.ALWAYS);

        return statItem;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header");
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        // Left section - App info
        HBox appInfo = new HBox(15);
        appInfo.setAlignment(Pos.CENTER_LEFT);

        VBox titleGroup = new VBox(3);
        Label title = new Label("TextScope");
        title.getStyleClass().add("app-title");

        Label subtitle = new Label("Multi-Threaded Text Analysis Platform");
        subtitle.getStyleClass().add("app-subtitle");

        titleGroup.getChildren().addAll(title, subtitle);
        appInfo.getChildren().addAll(titleGroup);

        // Center section - Quick actions
        HBox quickActions = new HBox(10);
        quickActions.setAlignment(Pos.CENTER);
        quickActions.getStyleClass().add("quick-actions");

        Button newProjectBtn = createHeaderButton("🆕 New Project");
        Button templatesBtn = createHeaderButton("📋 Templates");
        Button exportBtn = createHeaderButton("📤 Export");

        quickActions.getChildren().addAll(newProjectBtn, templatesBtn, exportBtn);

        // Right section - User info and controls
        HBox userSection = new HBox(15);
        userSection.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(userSection, Priority.ALWAYS);

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("Search analyses...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(180);

        // User profile
        HBox userProfile = new HBox(8);
        userProfile.setAlignment(Pos.CENTER_RIGHT);
        userProfile.getStyleClass().add("user-profile");

        StackPane userAvatar = new StackPane();
        userAvatar.getStyleClass().add("user-avatar");
        Label userInitial = new Label("U");
        userInitial.getStyleClass().add("user-initial");
        userAvatar.getChildren().add(userInitial);

        VBox userInfo = new VBox(2);
        Label userName = new Label("Current User");
        userName.getStyleClass().add("user-name");
        Label userRole = new Label("Analyst");
        userRole.getStyleClass().add("user-role");
        userInfo.getChildren().addAll(userName, userRole);

        userProfile.getChildren().addAll(userInfo, userAvatar);

        userSection.getChildren().addAll(searchField, userProfile);

        header.getChildren().addAll(appInfo, quickActions, userSection);

        return header;
    }

    private Button createHeaderButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("header-btn");
        return button;
    }

    // The rest of your existing methods remain the same...
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

        Label footerText = new Label("TextScope - Concurrency Programming Project © 2025-2026");
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