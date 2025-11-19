package com.example.textanalysisapp.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;

public class HelloApplication extends Application {   // ⬅️ هنا تغيّر الاسم

    @Override
    public void start(Stage primaryStage) {

        Button loadBtn = new Button("Load Files");
        Button startBtn = new Button("Start Analysis");

        TableView<FileInfo> table = new TableView<>();

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

        loadBtn.setOnAction(e -> {
            List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);

            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    long fileSize = file.length() / 1024;
                    String lastModified = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm")
                            .format(file.lastModified());

                    table.getItems().add(new FileInfo(
                            fileName,
                            fileSize,
                            lastModified,
                            file.getAbsolutePath()
                    ));
                }
            }
        });

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(loadBtn, startBtn, table);

        Scene scene = new Scene(layout, 800, 500);

        primaryStage.setTitle("Text Analyzer – Sprint 1");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
