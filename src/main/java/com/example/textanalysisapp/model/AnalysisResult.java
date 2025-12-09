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
        this.readingTime = new SimpleDoubleProperty(0.0);
        this.sentiment = new SimpleStringProperty("Neutral");
    }

    // Getters for properties
    public String getFileName() {
        return fileName.get();
    }

    public SimpleStringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public int getTotalWords() {
        return totalWords.get();
    }

    public SimpleIntegerProperty totalWordsProperty() {
        return totalWords;
    }

    public void setTotalWords(int totalWords) {
        this.totalWords.set(totalWords);
    }

    public int getUniqueWords() {
        return uniqueWords.get();
    }

    public SimpleIntegerProperty uniqueWordsProperty() {
        return uniqueWords;
    }

    public void setUniqueWords(int uniqueWords) {
        this.uniqueWords.set(uniqueWords);
    }

    public String getMostFrequent() {
        return mostFrequent.get();
    }

    public SimpleStringProperty mostFrequentProperty() {
        return mostFrequent;
    }

    public void setMostFrequent(String mostFrequent) {
        this.mostFrequent.set(mostFrequent);
    }

    public int getCharacterCount() {
        return characterCount.get();
    }

    public SimpleIntegerProperty characterCountProperty() {
        return characterCount;
    }

    public void setCharacterCount(int characterCount) {
        this.characterCount.set(characterCount);
    }

    public double getReadingTime() {
        return readingTime.get();
    }

    public SimpleDoubleProperty readingTimeProperty() {
        return readingTime;
    }

    public void setReadingTime(double readingTime) {
        this.readingTime.set(readingTime);
    }

    public String getSentiment() {
        return sentiment.get();
    }

    public SimpleStringProperty sentimentProperty() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment.set(sentiment);
    }
}