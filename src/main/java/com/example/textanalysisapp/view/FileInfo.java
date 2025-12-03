package com.example.textanalysisapp.view;

import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;// adi bah kima tesra update 3la attrebute f table direct ybdelha mach yastene 7ata ndir refresh

public class FileInfo {
    private SimpleStringProperty name;
    private SimpleLongProperty size;
    private SimpleStringProperty lastModified;
    private SimpleStringProperty path;

    public FileInfo(String name, long size, String lastModified, String path) {
        this.name = new SimpleStringProperty(name);
        this.size = new SimpleLongProperty(size);
        this.lastModified = new SimpleStringProperty(lastModified);
        this.path = new SimpleStringProperty(path);
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
}