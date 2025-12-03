package com.example.textanalysisapp.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TextAnalyzer {

    public static Map<String, Integer> analyze(Path filePath) throws IOException {
        String content = "";

        // Get file extension
        String fileName = filePath.toString().toLowerCase();

        if (fileName.endsWith(".pdf")) {
            // For PDF files, you would need a PDF library
            // Here's a placeholder for PDF handling
            content = extractTextFromPDF(filePath);
        } else if (fileName.endsWith(".docx") || fileName.endsWith(".doc")) {
            // For Word documents
            content = extractTextFromWord(filePath);
        } else if (fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            // For Excel files
            content = extractTextFromExcel(filePath);
        } else if (fileName.endsWith(".pptx") || fileName.endsWith(".ppt")) {
            // For PowerPoint files
            content = extractTextFromPowerPoint(filePath);
        } else if (fileName.endsWith(".txt") || fileName.endsWith(".csv") ||
                fileName.endsWith(".html") || fileName.endsWith(".htm") ||
                fileName.endsWith(".xml") || fileName.endsWith(".json") ||
                fileName.endsWith(".md") || fileName.endsWith(".rtf")) {
            // For plain text and markup files
            content = Files.readString(filePath);
        } else {
            // Try to read as plain text for unknown file types
            content = tryReadAsText(filePath);
        }

        return analyzeText(content);
    }

    private static Map<String, Integer> analyzeText(String content) {
        if (content == null || content.isEmpty()) {
            return new HashMap<>();
        }

        // Clean and split text
        content = content.toLowerCase()
                .replaceAll("[^a-zA-Z0-9 ]", " ")
                .replaceAll("\\s+", " ");

        String[] words = content.split("\\s+");

        Map<String, Integer> frequency = new HashMap<>();

        for (String word : words) {
            if (!word.isEmpty() && word.length() > 1) { // Filter out single characters
                frequency.put(word, frequency.getOrDefault(word, 0) + 1);
            }
        }

        return frequency;
    }

    // Placeholder methods for different file types
    private static String extractTextFromPDF(Path filePath) throws IOException {
        // You would need to add a PDF library like Apache PDFBox
        // Example with PDFBox (you would need to add the dependency):
        /*
        try (PDDocument document = PDDocument.load(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
        */
        throw new UnsupportedOperationException("PDF processing requires Apache PDFBox library");
    }

    private static String extractTextFromWord(Path filePath) throws IOException {
        // You would need Apache POI for Word documents
        // Example with POI (you would need to add the dependency):
        /*
        FileInputStream fis = new FileInputStream(filePath.toFile());
        XWPFDocument document = new XWPFDocument(fis);
        StringBuilder text = new StringBuilder();
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            text.append(paragraph.getText()).append("\n");
        }
        document.close();
        fis.close();
        return text.toString();
        */
        throw new UnsupportedOperationException("Word processing requires Apache POI library");
    }

    private static String extractTextFromExcel(Path filePath) throws IOException {
        // Using Apache POI for Excel
        /*
        FileInputStream fis = new FileInputStream(filePath.toFile());
        Workbook workbook = new XSSFWorkbook(fis); // for .xlsx
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Sheet sheet = workbook.getSheetAt(i);
            for (Row row : sheet) {
                for (Cell cell : row) {
                    text.append(cell.toString()).append(" ");
                }
                text.append("\n");
            }
        }
        workbook.close();
        fis.close();
        return text.toString();
        */
        throw new UnsupportedOperationException("Excel processing requires Apache POI library");
    }

    private static String extractTextFromPowerPoint(Path filePath) throws IOException {
        // Using Apache POI for PowerPoint
        /*
        FileInputStream fis = new FileInputStream(filePath.toFile());
        XMLSlideShow ppt = new XMLSlideShow(fis);
        StringBuilder text = new StringBuilder();

        for (XSLFSlide slide : ppt.getSlides()) {
            for (XSLFShape shape : slide.getShapes()) {
                if (shape instanceof XSLFTextShape) {
                    XSLFTextShape textShape = (XSLFTextShape) shape;
                    text.append(textShape.getText()).append("\n");
                }
            }
        }
        ppt.close();
        fis.close();
        return text.toString();
        */
        throw new UnsupportedOperationException("PowerPoint processing requires Apache POI library");
    }

    private static String tryReadAsText(Path filePath) {
        try {
            // Try to read as plain text with UTF-8 encoding
            byte[] bytes = Files.readAllBytes(filePath);
            return new String(bytes, "UTF-8");
        } catch (Exception e) {
            return "Unable to read file as text. File type might not be supported.";
        }
    }

    public static int totalWords(Map<String, Integer> freq) {
        return freq.values().stream().mapToInt(i -> i).sum();
    }

    public static int uniqueWords(Map<String, Integer> freq) {
        return freq.size();
    }

    // Additional utility methods
    public static Map<String, Integer> analyze(String text) {
        return analyzeText(text);
    }

    public static List<String> getMostFrequentWords(Map<String, Integer> freq, int limit) {
        return freq.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }

    public static Map<String, Double> getWordFrequenciesPercent(Map<String, Integer> freq) {
        int total = totalWords(freq);
        Map<String, Double> percentages = new HashMap<>();

        for (Map.Entry<String, Integer> entry : freq.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / total;
            percentages.put(entry.getKey(), percentage);
        }

        return percentages;
    }
}