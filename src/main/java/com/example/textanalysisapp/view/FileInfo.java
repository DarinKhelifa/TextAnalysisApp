package com.example.textanalysisapp.view;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class FileInfo {
    private SimpleStringProperty name;
    private SimpleLongProperty size;
    private SimpleStringProperty lastModified;
    private SimpleStringProperty path;
    private SimpleStringProperty fileType;
    private SimpleStringProperty status;
    private String fileContent; // Store file content for preview

    public FileInfo(String name, long size, String lastModified, String path) {
        this.name = new SimpleStringProperty(name);
        this.size = new SimpleLongProperty(size);
        this.lastModified = new SimpleStringProperty(lastModified);
        this.path = new SimpleStringProperty(path);
        this.fileType = new SimpleStringProperty(getFileTypeFromName(name));
        this.status = new SimpleStringProperty("pending");
        this.fileContent = "";
    }

    private String getFileTypeFromName(String fileName) {
        if (fileName.toLowerCase().endsWith(".txt")) return "txt";
        if (fileName.toLowerCase().endsWith(".md")) return "md";
        if (fileName.toLowerCase().endsWith(".java")) return "java";
        if (fileName.toLowerCase().endsWith(".xml")) return "xml";
        if (fileName.toLowerCase().endsWith(".json")) return "json";
        if (fileName.toLowerCase().endsWith(".csv")) return "csv";
        if (fileName.toLowerCase().endsWith(".html") || fileName.toLowerCase().endsWith(".htm")) return "html";
        return "other";
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public long getSize() {
        return size.get();
    }

    public SimpleLongProperty sizeProperty() {
        return size;
    }

    public String getLastModified() {
        return lastModified.get();
    }

    public SimpleStringProperty lastModifiedProperty() {
        return lastModified;
    }

    public String getPath() {
        return path.get();
    }

    public SimpleStringProperty pathProperty() {
        return path;
    }

    public String getFileType() {
        return fileType.get();
    }

    public SimpleStringProperty fileTypeProperty() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType.set(fileType);
    }

    public String getStatus() {
        return status.get();
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }

    public String getFileContent() {
        return fileContent;
    }

    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }
}