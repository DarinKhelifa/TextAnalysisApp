package com.example.textanalysisapp.model;

import java.util.*;

public class TextAnalyzer {

    /**
     * Main analysis method
     */
    public Map<String, Object> analyzeText(String content) {
        Map<String, Object> results = new HashMap<>();

        if (content == null || content.trim().isEmpty()) {
            return getEmptyResults();
        }

        // 1. Split text into words (delay 300ms)
        try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        String[] words = content.split("\\s+");
        int totalWords = words.length;
        results.put("totalWords", totalWords);

        // 2. Count unique words (delay 300ms)
        try { Thread.sleep(300); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        Set<String> uniqueWords = new HashSet<>();
        for (String word : words) {
            String cleanWord = word.toLowerCase().replaceAll("[^a-z]", "");
            if (!cleanWord.isEmpty()) {
                uniqueWords.add(cleanWord);
            }
        }
        results.put("uniqueWords", uniqueWords.size());

        // 3. Count characters (delay 200ms)
        try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        int charsWithSpaces = content.length();
        int charsWithoutSpaces = content.replace(" ", "").length();
        results.put("charsWithSpaces", charsWithSpaces);
        results.put("charsWithoutSpaces", charsWithoutSpaces);

        // 4. Most frequent words (delay 400ms)
        try { Thread.sleep(400); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        Map<String, Integer> wordFrequency = calculateWordFrequency(words);
        String topWords = getTopWords(wordFrequency, 5);
        results.put("mostFrequent", topWords);

        // 5. Estimated reading time
        double readingTime = totalWords / 200.0;
        results.put("readingTime", String.format("%.1f", readingTime));

        // 6. Sentence count
        int sentenceCount = content.split("[.!?]+").length;
        results.put("sentenceCount", sentenceCount);

        return results;
    }

    /**
     * Calculate word frequency
     */
    private Map<String, Integer> calculateWordFrequency(String[] words) {
        Map<String, Integer> frequency = new HashMap<>();
        for (String word : words) {
            String cleanWord = word.toLowerCase().replaceAll("[^a-z]", "");
            if (!cleanWord.isEmpty()) {
                frequency.put(cleanWord, frequency.getOrDefault(cleanWord, 0) + 1);
            }
        }
        return frequency;
    }

    /**
     * Get top N frequent words
     */
    private String getTopWords(Map<String, Integer> frequency, int limit) {
        StringBuilder result = new StringBuilder();

        // Convert map to list for sorting
        List<Map.Entry<String, Integer>> list = new ArrayList<>(frequency.entrySet());

        // Sort descending by frequency
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Take first (limit) elements
        int count = 0;
        for (Map.Entry<String, Integer> entry : list) {
            if (count >= limit) break;
            result.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            count++;
        }

        return result.toString();
    }

    /**
     * Empty results for empty content
     */
    private Map<String, Object> getEmptyResults() {
        Map<String, Object> results = new HashMap<>();
        results.put("totalWords", 0);
        results.put("uniqueWords", 0);
        results.put("charsWithSpaces", 0);
        results.put("charsWithoutSpaces", 0);
        results.put("mostFrequent", "No words found");
        results.put("readingTime", "0.0");
        results.put("sentenceCount", 0);
        return results;
    }
}