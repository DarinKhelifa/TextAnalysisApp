package com.example.textanalysisapp.view;

import javafx.beans.property.*;

public class FileInfo {
    private final StringProperty name;
    private final StringProperty size;  // ⚠️ استخدم String بدلاً من Long
    private final StringProperty lastModified;
    private final StringProperty path;
    private final StringProperty status;

    public FileInfo(String name, long sizeKB, String lastModified, String path) {
        this.name = new SimpleStringProperty(name);
        // أضف "KB" مباشرة في السلسلة
        this.size = new SimpleStringProperty(formatSize(sizeKB));
        this.lastModified = new SimpleStringProperty(lastModified);
        this.path = new SimpleStringProperty(path);
        this.status = new SimpleStringProperty("Pending");
    }

    private String formatSize(long sizeKB) {
        if (sizeKB == 0) {
            return "< 1 KB";
        } else {
            return sizeKB + " KB";
        }
    }

    // Getters
    public String getName() { return name.get(); }
    public String getSize() { return size.get(); }  // ⚠️ الآن String
    public String getLastModified() { return lastModified.get(); }
    public String getPath() { return path.get(); }
    public String getStatus() { return status.get(); }

    // Setters
    public void setStatus(String s) { status.set(s); }

    // Properties
    public StringProperty nameProperty() { return name; }
    public StringProperty sizeProperty() { return size; }  // ⚠️ StringProperty
    public StringProperty lastModifiedProperty() { return lastModified; }
    public StringProperty pathProperty() { return path; }
    public StringProperty statusProperty() { return status; }
}