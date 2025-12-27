package com.example.textanalysisapp.model;

import java.util.*;
import java.util.stream.Collectors;

public class TextAnalyzer {

    /**
     * Main analysis method - pure business logic
     */
    public Map<String, Object> analyzeText(String content) {
        Map<String, Object> results = new HashMap<>();

        if (content == null || content.trim().isEmpty()) {
            return getEmptyResults();
        }

        // Add artificial delay for realistic progress
       try {
            Thread.sleep(500); // 0.5 second delay to show progress
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 1. Basic statistics
        String[] words = content.split("\\s+");
        int totalWords = words.length;
        results.put("totalWords", totalWords);

        // Add delay between steps
        addDelay(300);

        // 2. Unique words calcul
        Set<String> uniqueWords = Arrays.stream(words)
                .map(word -> word.toLowerCase().replaceAll("[^a-zA-Z0-9]", ""))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toSet());
        results.put("uniqueWords", uniqueWords.size());

        addDelay(300);// time to update progress bar

        // 3. Character counts
        int charsWithSpaces = content.length();
        int charsWithoutSpaces = content.replace(" ", "").length();
        results.put("charsWithSpaces", charsWithSpaces);
        results.put("charsWithoutSpaces", charsWithoutSpaces);

        addDelay(300);

        // 4. Most frequent words
        Map<String, Integer> wordFrequency = calculateWordFrequency(words);
        String topWords = getTopWords(wordFrequency, 10);
        results.put("mostFrequent", topWords);

        // Store top words for highlighting
        List<String> topWordsList = getTopWordsList(wordFrequency, 10);
        results.put("topWordsList", topWordsList);

        addDelay(300);

        // 5. Reading time
        double readingTime = totalWords / 200.0;
        results.put("readingTime", String.format("%.1f", readingTime));

        // 6. Sentence count
        int sentenceCount = content.split("[.!?]+").length;
        results.put("sentenceCount", sentenceCount);

        // 7. Average word length
        double avgWordLength = totalWords > 0 ?
                (double) charsWithoutSpaces / totalWords : 0;
        results.put("avgWordLength", String.format("%.2f", avgWordLength));

        // 8. Sentiment
        String sentiment = analyzeSentiment(content);
        results.put("sentiment", sentiment);

        // 9. Paragraph count
        int paragraphCount = content.split("\\n\\s*\\n").length;
        results.put("paragraphCount", paragraphCount);

        // 10. Longest word
        String longestWord = findLongestWord(words);
        results.put("longestWord", longestWord);

        return results;
    }

    private void addDelay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Calculate word frequency
     */
    private Map<String, Integer> calculateWordFrequency(String[] words) {
        Map<String, Integer> frequency = new HashMap<>();
        for (String word : words) {
            String cleanWord = word.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
            if (!cleanWord.isEmpty() && cleanWord.length() > 1) {
                frequency.put(cleanWord, frequency.getOrDefault(cleanWord, 0) + 1);
            }
        }
        return frequency;
    }

    /**
     * Get top N frequent words as string
     */
    private String getTopWords(Map<String, Integer> frequency, int limit) {
        return frequency.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .collect(Collectors.joining(", "));
    }

    /**
     * Get top N frequent words as list
     */
    private List<String> getTopWordsList(Map<String, Integer> frequency, int limit) {
        return frequency.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Basic sentiment analysis
     */
    private String analyzeSentiment(String text) {
        String[] positiveWords = {"good", "great", "excellent", "happy", "love", "best", "nice", "awesome", "positive", "wonderful", "perfect", "amazing"};
        String[] negativeWords = {"bad", "terrible", "awful", "sad", "hate", "worst", "poor", "horrible", "negative", "dislike", "angry", "upset"};

        text = text.toLowerCase();
        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : positiveWords) {
            if (text.contains(word)) positiveCount++;
        }
        for (String word : negativeWords) {
            if (text.contains(word)) negativeCount++;
        }

        if (positiveCount > negativeCount) return "Positive (" + positiveCount + " positive, " + negativeCount + " negative)";
        if (negativeCount > positiveCount) return "Negative (" + positiveCount + " positive, " + negativeCount + " negative)";
        return "Neutral (" + positiveCount + " positive, " + negativeCount + " negative)";
    }

    /**
     * Find longest word
     */
    private String findLongestWord(String[] words) {
        String longest = "";
        for (String word : words) {
            String cleanWord = word.replaceAll("[^a-zA-Z0-9]", "");
            if (cleanWord.length() > longest.length()) {
                longest = cleanWord;
            }
        }
        return longest + " (" + longest.length() + " characters)";
    }

    /**
     * Return empty results for empty content
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
        results.put("avgWordLength", "0.00");
        results.put("sentiment", "Neutral");
        results.put("paragraphCount", 0);
        results.put("longestWord", "None");
        results.put("topWordsList", new ArrayList<String>());
        return results;
    }
}