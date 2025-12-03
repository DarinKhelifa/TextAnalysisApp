package com.example.textanalysisapp.model;

import java.util.*;
import java.util.stream.Collectors;

public class TextAnalyzer {

    /**
     * Analyze text and return a map of results
     * @param content The text content to analyze
     * @return Map containing all analysis statistics
     */
    public Map<String, Object> analyzeText(String content) {
        Map<String, Object> results = new HashMap<>();

        if (content == null || content.trim().isEmpty()) {
            // Return empty results for empty content
            results.put("totalWords", 0);
            results.put("uniqueWords", 0);
            results.put("charsWithSpaces", 0);
            results.put("charsWithoutSpaces", 0);
            results.put("mostFrequent", "No words found");
            results.put("readingTime", "0.0");
            results.put("sentenceCount", 0);
            results.put("avgWordLength", "0.00");
            results.put("sentiment", "Neutral");
            return results;
        }

        // 1. Word count
        String[] words = content.split("\\s+");
        int totalWords = words.length;
        results.put("totalWords", totalWords);

        // 2. Unique words
        Set<String> uniqueWords = Arrays.stream(words)
                .map(word -> word.toLowerCase().replaceAll("[^a-zA-Z]", ""))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toSet());
        results.put("uniqueWords", uniqueWords.size());

        // 3. Character counts
        int charsWithSpaces = content.length();
        int charsWithoutSpaces = content.replace(" ", "").length();
        results.put("charsWithSpaces", charsWithSpaces);
        results.put("charsWithoutSpaces", charsWithoutSpaces);

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
        results.put("mostFrequent", topWords.isEmpty() ? "No words found" : topWords);

        // 5. Reading time (200 words per minute)
        double readingTime = totalWords / 200.0;
        results.put("readingTime", String.format("%.1f", readingTime));

        // 6. Sentence count (simple approximation)
        int sentenceCount = content.split("[.!?]+").length;
        results.put("sentenceCount", sentenceCount);

        // 7. Average word length
        double avgWordLength = words.length > 0 ?
                (double) charsWithoutSpaces / words.length : 0;
        results.put("avgWordLength", String.format("%.2f", avgWordLength));

        // 8. Basic sentiment analysis
        String sentiment = analyzeSentiment(content);
        results.put("sentiment", sentiment);

        return results;
    }

    /**
     * Basic sentiment analysis
     */
    private String analyzeSentiment(String text) {
        String[] positiveWords = {"good", "great", "excellent", "happy", "love", "best", "nice", "awesome", "positive"};
        String[] negativeWords = {"bad", "terrible", "awful", "sad", "hate", "worst", "poor", "horrible", "negative"};

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

    /**
     * Utility method to get total words from a frequency map
     */
    public static int totalWords(Map<String, Integer> freq) {
        return freq.values().stream().mapToInt(i -> i).sum();
    }

    /**
     * Utility method to get unique word count from a frequency map
     */
    public static int uniqueWords(Map<String, Integer> freq) {
        return freq.size();
    }

    /**
     * Get most frequent words from a frequency map
     */
    public static List<String> getMostFrequentWords(Map<String, Integer> freq, int limit) {
        return freq.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}