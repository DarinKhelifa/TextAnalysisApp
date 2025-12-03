package com.example.textanalysisapp.model;

import javafx.beans.property.*;

public class AnalysisResult {
    private SimpleStringProperty fileName;
    private SimpleIntegerProperty totalWords;
    private SimpleIntegerProperty uniqueWords;
    private SimpleStringProperty mostFrequent;
    private SimpleIntegerProperty characterCount;
    private SimpleDoubleProperty readingTime;
    private SimpleStringProperty sentiment;

    public AnalysisResult(String fileName) {
        this.fileName = new SimpleStringProperty(fileName);
        this.totalWords = new SimpleIntegerProperty(0);
        this.uniqueWords = new SimpleIntegerProperty(0);
        this.mostFrequent = new SimpleStringProperty("");
        this.characterCount = new SimpleIntegerProperty(0);
        this.readingTime = new SimpleDoubleProperty(0);
        this.sentiment = new SimpleStringProperty("Neutral");
    }

    // Getters and property methods...
}