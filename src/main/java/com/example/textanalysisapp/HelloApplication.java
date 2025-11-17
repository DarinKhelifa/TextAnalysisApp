package com.example.textanalysisapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;



public class HelloApplication extends Application {
    @Override
    public void start(Stage primaryStage) {

        // الأزرار
        Button loadBtn = new Button("Load Files");
        Button startBtn = new Button("Start Analysis");

        // لائحة باش نعرضو الملفات
        ListView<String> fileList = new ListView<>();

        // Layout
        VBox layout = new VBox(15);// vertical arrangement of  elements with 15 px between each two elements
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(loadBtn, startBtn, fileList);

        Scene scene = new Scene(layout, 600, 400);

        FileChooser fileChooser = new FileChooser();

        loadBtn.setOnAction(e -> {
            List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);

            if (files != null) {
                for (File file : files) {
                    fileList.getItems().add(file.getAbsolutePath());
                }
            }
        });


        primaryStage.setTitle("Text Analyzer – Sprint 1");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
