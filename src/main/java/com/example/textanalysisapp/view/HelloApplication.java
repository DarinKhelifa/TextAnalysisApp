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

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Header Label
        Label headerLabel = new Label("Text Analyzer");
        headerLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #192a51;");

        // Load logo image safely
        ImageView logoView = new ImageView();
        URL logoUrl = getClass().getClassLoader().getResource("images/logo.png");
        if (logoUrl != null) {
            Image logoImage = new Image(logoUrl.toExternalForm());
            logoView.setImage(logoImage);
            logoView.setFitWidth(40);
            logoView.setPreserveRatio(true);
        } else {
            System.out.println("Logo image not found!");
        }

        // Header Box containing logo + label
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.getChildren().addAll(logoView, headerLabel);
        headerBox.setPadding(new Insets(15));
        headerBox.setStyle("-fx-background-color: #f5e6e8;");

        // Buttons
        Button loadBtn = new Button("Load Files");

        loadBtn.getStyleClass().addAll("styled-button", "load-button");

        Button startBtn = new Button("Start Analysis");
        startBtn.getStyleClass().addAll("styled-button", "start-button");

        Button deleteBtn = new Button("Delete Selected");
        deleteBtn.getStyleClass().addAll("styled-button", "delete-button");



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

        // Search field (مصغر ومركزي)
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name...");
        searchField.setPrefWidth(200);   // عرض أصغر
        searchField.setPrefHeight(25);   // ارتفاع أصغر
        searchField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #d5c6e0; " +
                        "-fx-border-radius: 5; " +
                        "-fx-background-radius: 5; " +
                        "-fx-padding: 3;"
        );

        // توسيط شريط البحث
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
        HBox buttonsBox = new HBox(10, loadBtn, startBtn, deleteBtn);
        buttonsBox.setPadding(new Insets(10, 0, 10, 0));

        VBox layout = new VBox(10);
        layout.setStyle("-fx-background-color: #f5e6e8;");
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(headerBox, searchBox, buttonsBox, table);
        VBox.setVgrow(table, Priority.ALWAYS);

        Scene scene = new Scene(layout, 900, 500);
        loadExternalCSS(scene);

        primaryStage.setTitle("Text Analyzer – Sprint 1");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadExternalCSS(Scene scene) {
        try {
            URL cssUrl = getClass().getClassLoader().getResource("CSS/Style.css");

            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                return;
            }
            File cssFile = new File("src/main/resources/CSS/Style.css");
            if (cssFile.exists()) {
                scene.getStylesheets().add(cssFile.toURI().toString());
                return;
            }
            cssFile = new File("resources/CSS/Style.css");

            if (cssFile.exists()) {
                scene.getStylesheets().add(cssFile.toURI().toString());
                return;
            }
            System.out.println("CSS file not found. Using inline styles only.");
        } catch (Exception e) {
            System.out.println("Error loading CSS: " + e.getMessage());
        }
    }
}