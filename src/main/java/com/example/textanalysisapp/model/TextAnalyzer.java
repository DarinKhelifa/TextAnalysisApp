package com.example.textanalysisapp.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TextAnalyzer {

    public static Map<String, Integer> analyze(Path filePath) throws IOException {
        String content = Files.readString(filePath);

        content = content.toLowerCase().replaceAll("[^a-zA-Z ]", " ");
        String[] words = content.split("\\s+");

        Map<String, Integer> frequency = new HashMap<>();

        for (String word : words) {
            if (!word.isEmpty()) {
                frequency.put(word, frequency.getOrDefault(word, 0) + 1);
            }
        }

        return frequency;
    }

    public static int totalWords(Map<String, Integer> freq) {
        return freq.values().stream().mapToInt(i -> i).sum();
    }

    public static int uniqueWords(Map<String, Integer> freq) {
        return freq.size();
    }
}
