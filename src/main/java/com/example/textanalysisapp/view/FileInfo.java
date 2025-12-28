package com.example.textanalysisapp.view;

import javafx.beans.property.*;

public class FileInfo {
    private final StringProperty name;
    private final LongProperty size;
    private final StringProperty path;
    private final StringProperty status;

    public FileInfo(String name, long size, String lastModified, String path) {
        this.name = new SimpleStringProperty(name);
        this.size = new SimpleLongProperty(size);
        this.path = new SimpleStringProperty(path);
        this.status = new SimpleStringProperty("Pending");
    }

    // Getters
    public String getName() { return name.get(); }
    public long getSize() { return size.get(); }
    public String getPath() { return path.get(); }
    public String getStatus() { return status.get(); }

    // Setters
    public void setStatus(String s) { status.set(s); }

    // Properties for TableView
    public StringProperty nameProperty() { return name; }
    public LongProperty sizeProperty() { return size; }
    public StringProperty pathProperty() { return path; }
    public StringProperty statusProperty() { return status; }
}