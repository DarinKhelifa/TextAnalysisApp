package com.example.textanalysisapp.view;

import com.example.textanalysisapp.controller.AnalysisController;
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
import javafx.scene.text.TextAlignment;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {// adi nogtat el bidaia ta3 app
        // Header with Logo and App Name
        HBox headerBox = createHeader(); //

        // App Description
        Label descriptionLabel = new Label("Analyze your text files with powerful insights and statistics");
        descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #192a51; -fx-font-style: italic;");
        descriptionLabel.setAlignment(Pos.CENTER);
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);

        // Buttons (bigger and centered)
        Button loadBtn = new Button("Load Files");
        loadBtn.setStyle("-fx-background-color: #d5c6e0; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8;");
        loadBtn.setPrefSize(140, 40);

        Button startBtn = new Button("Start Analysis");
        startBtn.setStyle("-fx-background-color: #aaa1c8; -fx-text-fill: #192a51; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8;");
        startBtn.setPrefSize(140, 40);

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.setStyle("-fx-background-color: #967aa1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20 10 20; -fx-background-radius: 8; -fx-border-radius: 8;");
        deleteBtn.setPrefSize(140, 40);

        // Add hover effects
        setupButtonHoverEffects(loadBtn, startBtn, deleteBtn);// tb3thom l style css

        // TableView
        TableView<FileInfo> table = new TableView<>();// create table for files
        table.setStyle("-fx-control-inner-background: #f5e6e8; -fx-background-color: #f5e6e8;");
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        ObservableList<FileInfo> masterData = FXCollections.observableArrayList();// takhzine el baianat ta3 file f table

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
                    masterData.add(new FileInfo(fileName, fileSize, lastModified, file.getAbsolutePath()));// yzidhom ll table
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

        // Center search bar
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
        HBox buttonsBox = new HBox(20, loadBtn, startBtn, deleteBtn);  // to make the bottons in the center
        buttonsBox.setPadding(new Insets(15, 0, 15, 0));
        buttonsBox.setAlignment(Pos.CENTER);
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setVisible(false);

        Label totalWordsLabel = new Label("Total Words: ");
        Label uniqueWordsLabel = new Label("Unique Words: ");

        TextArea frequentWordsArea = new TextArea();
        frequentWordsArea.setEditable(false);
        frequentWordsArea.setPromptText("Most frequent words...");

        VBox resultBox = new VBox(10,
                progressBar,
                totalWordsLabel,
                uniqueWordsLabel,
                frequentWordsArea
        );
        resultBox.setPadding(new Insets(10));


        // ✅ CONTROLLER CONNECTION (هاد السطر كان ناقص عندك)
        AnalysisController controller = new AnalysisController(
                progressBar,
                totalWordsLabel,
                uniqueWordsLabel,
                frequentWordsArea
        );

        // ✅ Start Analysis Button Fix (هاد السطر هو سبب المشكل)
        startBtn.setOnAction(e -> {
            FileInfo selectedFile = table.getSelectionModel().getSelectedItem();
            controller.startAnalysis(selectedFile);
        });



        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: #f5e6e8;");
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(
                headerBox,
                descriptionLabel,
                searchBox,
                buttonsBox,
                resultBox,
                table
        );
        VBox.setVgrow(table, Priority.ALWAYS);

        Scene scene = new Scene(layout, 900, 550);
        loadExternalCSS(scene); // Still try to load CSS for other styles

        primaryStage.setTitle("VioletLens - Text Analyzer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

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
        // Load logo image safely - FIXED PATH
        ImageView logoView = new ImageView();
        try {
            // Try multiple possible paths for the logo
            URL logoUrl = getClass().getResource("/images/logo.png");
         /*   if (logoUrl == null) {
                logoUrl = getClass().getResource("images/logo.png");
            }*/
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
                // Fallback: create text logo if image not found
                System.out.println("Logo image not found! Using text fallback.");
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

        // App name label
        Label appNameLabel = createAppNameLabel();

        // Header Box containing logo + label
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
            // Try multiple possible paths for CSS
            URL cssUrl = getClass().getResource("/CSS/Style.css");
            if (cssUrl == null) {
                cssUrl = getClass().getResource("CSS/Style.css");
            }
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