package com.example.textanalysisapp.view;

public class FileInfo {
    private String name;
    private long size;
    private String lastModified;
    private String path;

    public FileInfo(String name, long size, String lastModified, String path) {
        this.name = name;
        this.size = size;
        this.lastModified = lastModified;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }

    public String getLastModified() {
        return lastModified;
    }

    public String getPath() {
        return path;
    }
}
