package com.example.textanalysisapp.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create styled buttons
        Button loadBtn = createStyledButton("📁 Load Files", "#4CAF50");
        Button startBtn = createStyledButton("🚀 Start Analysis", "#2196F3");

        // Disable start button initially (no files loaded)
        startBtn.setDisable(true);

        // Create styled table
        TableView<FileInfo> table = createStyledTable();

        // Configure columns
        TableColumn<FileInfo, String> nameCol = new TableColumn<>("File Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<FileInfo, Long> sizeCol = new TableColumn<>("Size (KB)");
        sizeCol.setCellValueFactory(new PropertyValueFactory<>("size"));
        sizeCol.setPrefWidth(100);

        TableColumn<FileInfo, String> dateCol = new TableColumn<>("Last Modified");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("lastModified"));
        dateCol.setPrefWidth(150);

        TableColumn<FileInfo, String> pathCol = new TableColumn<>("Path");
        pathCol.setCellValueFactory(new PropertyValueFactory<>("path"));
        pathCol.setPrefWidth(300);

        table.getColumns().addAll(nameCol, sizeCol, dateCol, pathCol);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Text Files for Analysis");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt", "*.doc", "*.docx", "*.pdf")
        );

        loadBtn.setOnAction(e -> {
            List<File> files = fileChooser.showOpenMultipleDialog(primaryStage);

            if (files != null && !files.isEmpty()) {
                table.getItems().clear(); // Clear previous selections
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
                startBtn.setDisable(false); // Enable start button when files are loaded

                // Add success feedback
                loadBtn.setText("✓ Files Loaded (" + files.size() + ")");
                loadBtn.setStyle("-fx-background-color: #45a049; -fx-text-fill: white;");
            }
        });

        startBtn.setOnAction(e -> {
            if (!table.getItems().isEmpty()) {
                // Visual feedback
                startBtn.setText("⏳ Analyzing...");
                startBtn.setStyle("-fx-background-color: #1976D2; -fx-text-fill: white;");

                // Simulate analysis process
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                javafx.application.Platform.runLater(() -> {
                                    startBtn.setText("✅ Analysis Complete");
                                    startBtn.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white;");
                                });
                            }
                        },
                        2000
                );
            }
        });

        // Create header
        HBox header = createHeader();

        // Button container
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loadBtn, startBtn);

        // Main layout
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(25));
        layout.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");
        layout.getChildren().addAll(header, buttonBox, table);

        Scene scene = new Scene(layout, 900, 600);

        // Apply CSS styling - Safe method
        try {
            String cssPath = getClass().getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            System.out.println("CSS loaded successfully");
        } catch (Exception e) {
            System.err.println("CSS file not found. Using inline styles only.");
            // Apply inline table styles as fallback
            applyInlineTableStyles(table);
        }

        primaryStage.setTitle("📊 Text Analyzer – Sprint 1");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
        primaryStage.show();
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-font-size: 14px; " +
                        "-fx-padding: 12px 24px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2); " +
                        "-fx-cursor: hand;",
                color
        ));

        // Hover effect
        button.setOnMouseEntered(e -> {
            button.setStyle(button.getStyle().replace(
                    "dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2)",
                    "dropshadow(three-pass-box, rgba(0,0,0,0.5), 8, 0, 0, 3)"
            ));
        });

        button.setOnMouseExited(e -> {
            button.setStyle(button.getStyle().replace(
                    "dropshadow(three-pass-box, rgba(0,0,0,0.5), 8, 0, 0, 3)",
                    "dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2)"
            ));
        });

        return button;
    }

    private TableView<FileInfo> createStyledTable() {
        TableView<FileInfo> table = new TableView<>();
        table.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #e0e0e0; " +
                        "-fx-border-radius: 8px; " +
                        "-fx-background-radius: 8px; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 3);"
        );
        table.setPlaceholder(new javafx.scene.control.Label("📄 No files selected. Click 'Load Files' to begin."));
        return table;
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 10, 0));

        javafx.scene.control.Label title = new javafx.scene.control.Label("Text Analysis Dashboard");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);");

        javafx.scene.control.Label subtitle = new javafx.scene.control.Label("Upload and analyze your text files with ease");
        subtitle.setFont(Font.font("System", 14));
        subtitle.setTextFill(Color.WHITE);
        subtitle.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);");

        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.getChildren().addAll(title, subtitle);

        header.getChildren().add(titleBox);
        return header;
    }

    private void applyInlineTableStyles(TableView<FileInfo> table) {
        // Inline styles as fallback when CSS is not available
        table.setStyle(table.getStyle() +
                " -fx-table-header-border-color: transparent;" +
                " -fx-table-cell-border-color: transparent;");
    }
}