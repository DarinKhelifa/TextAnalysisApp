package com.example.textanalysisapp.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;

import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {

        // Buttons
        Button loadBtn = new Button("Load Files");
        Button startBtn = new Button("Start Analysis");
        Button deleteBtn = new Button("Delete Selected");

        // TableView
        TableView<FileInfo> table = new TableView<>();
<<<<<<< HEAD
=======
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); // يسمح باختيار متعدد
>>>>>>> maram
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

                    masterData.add(new FileInfo(
                            fileName,
                            fileSize,
                            lastModified,
                            file.getAbsolutePath()
                    ));
                }
            }
        });

<<<<<<< HEAD
        // 🔹 إضافة شريط البحث
=======
        // Delete Selected
        deleteBtn.setOnAction(e -> {
            ObservableList<FileInfo> selected = table.getSelectionModel().getSelectedItems();
            if (!selected.isEmpty()) {
                masterData.removeAll(selected);
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "No file selected!");
                alert.show();
            }
        });

        // Search
>>>>>>> maram
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name...");

        FilteredList<FileInfo> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(file -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return file.getName().toLowerCase().contains(lowerCaseFilter);
            });
        });

        SortedList<FileInfo> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

<<<<<<< HEAD
        HBox buttonsBox = new HBox(10, loadBtn, startBtn);
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(searchField, buttonsBox, table);

        Scene scene = new Scene(layout, 800, 500);
=======
        // Layout
        HBox buttonsBox = new HBox(10, loadBtn, startBtn, deleteBtn);
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(searchField, buttonsBox, table);
>>>>>>> maram

        Scene scene = new Scene(layout, 900, 500);
        primaryStage.setTitle("Text Analyzer – Sprint 1");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
