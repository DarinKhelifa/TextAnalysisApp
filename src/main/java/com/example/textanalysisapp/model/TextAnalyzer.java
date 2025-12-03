package com.example.textanalysisapp.model;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextAnalyzer {

    public AnalysisResult analyze(TextFile textFile) {
        String content = textFile.getContent();
        AnalysisResult result = new AnalysisResult(textFile.getFile().getName());

        // 1. Word count
        String[] words = content.split("\\s+");
        int totalWords = words.length;
        result.setTotalWords(totalWords);

        // 2. Unique words
        Set<String> uniqueWords = Arrays.stream(words)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        result.setUniqueWords(uniqueWords.size());

        // 3. Character count (with/without spaces)
        int charsWithSpaces = content.length();
        int charsWithoutSpaces = content.replace(" ", "").length();
        result.setCharacterCount(charsWithSpaces);

        // 4. Most frequent words
        Map<String, Integer> wordFrequency = new HashMap<>();
        for (String word : words) {
            String cleanWord = word.toLowerCase().replaceAll("[^a-zA-Z]", "");
            if (!cleanWord.isEmpty()) {
                wordFrequency.put(cleanWord, wordFrequency.getOrDefault(cleanWord, 0) + 1);
            }
        }

        String topWords = wordFrequency.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(5)
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.joining(", "));
        result.setMostFrequent(topWords);

        // 5. Reading time (200 words per minute)
        double readingTime = totalWords / 200.0;
        result.setReadingTime(Math.round(readingTime * 10.0) / 10.0);

        // 6. Sentiment analysis (basic)
        String sentiment = analyzeSentiment(content);
        result.setSentiment(sentiment);

        return result;
    }

    private String analyzeSentiment(String text) {
        // Simple dictionary-based sentiment
        String[] positiveWords = {"good", "great", "excellent", "happy", "love", "best"};
        String[] negativeWords = {"bad", "terrible", "awful", "sad", "hate", "worst"};

        text = text.toLowerCase();
        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : positiveWords) {
            if (text.contains(word)) positiveCount++;
        }
        for (String word : negativeWords) {
            if (text.contains(word)) negativeCount++;
        }

        if (positiveCount > negativeCount) return "Positive";
        if (negativeCount > positiveCount) return "Negative";
        return "Neutral";
    }
}