package com.example.textanalysisapp.model;

public class AnalysisResult {
    private String fileName;
    private int totalWords;
    private int uniqueWords;
    private String mostFrequent;
    private int characterCount;

    public AnalysisResult(String fileName) {
        this.fileName = fileName;
        this.totalWords = 0;
        this.uniqueWords = 0;
        this.mostFrequent = "";
        this.characterCount = 0;
    }

    // Getters
    public String getFileName() {
        return fileName;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public int getUniqueWords() {
        return uniqueWords;
    }

    public String getMostFrequent() {
        return mostFrequent;
    }

    public int getCharacterCount() {
        return characterCount;
    }

    // Setters
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setTotalWords(int totalWords) {
        this.totalWords = totalWords;
    }

    public void setUniqueWords(int uniqueWords) {
        this.uniqueWords = uniqueWords;
    }

    public void setMostFrequent(String mostFrequent) {
        this.mostFrequent = mostFrequent;
    }

    public void setCharacterCount(int characterCount) {
        this.characterCount = characterCount;
    }
}