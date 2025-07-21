package com.sau.summer.service;

import com.sau.summer.dto.SimilarityRequest;
import com.sau.summer.dto.SimilarityResponse;
import org.apache.commons.text.similarity.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for calculating text similarity using various algorithms
 */
@Service
public class SimilarityService {
    
    private final LevenshteinDistance levenshteinDistance;
    private final JaroWinklerSimilarity jaroWinklerSimilarity;
    private final CosineSimilarity cosineSimilarity;
    private final JaccardSimilarity jaccardSimilarity;
    
    public SimilarityService() {
        this.levenshteinDistance = new LevenshteinDistance();
        this.jaroWinklerSimilarity = new JaroWinklerSimilarity();
        this.cosineSimilarity = new CosineSimilarity();
        this.jaccardSimilarity = new JaccardSimilarity();
    }
    
    /**
     * Calculate similarity based on the specified algorithm
     */
    public SimilarityResponse calculateSimilarity(SimilarityRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            String text1 = preprocessText(request.getText1(), request);
            String text2 = preprocessText(request.getText2(), request);
            
            if ("all".equals(request.getAlgorithm())) {
                return calculateAllSimilarities(text1, text2, startTime);
            } else {
                double score = calculateSingleSimilarity(text1, text2, request.getAlgorithm());
                long processingTime = System.currentTimeMillis() - startTime;
                
                SimilarityResponse response = new SimilarityResponse(score, request.getAlgorithm(), processingTime);
                response.setMetadata(createMetadata(text1, text2, request));
                return response;
            }
        } catch (Exception e) {
            return SimilarityResponse.error("Error calculating similarity: " + e.getMessage());
        }
    }
    
    /**
     * Calculate similarity using all available algorithms
     */
    private SimilarityResponse calculateAllSimilarities(String text1, String text2, long startTime) {
        Map<String, Double> scores = new HashMap<>();
        
        scores.put("cosine", calculateCosineSimilarity(text1, text2));
        scores.put("jaccard", calculateJaccardSimilarity(text1, text2));
        scores.put("levenshtein", calculateLevenshteinSimilarity(text1, text2));
        scores.put("jaro_winkler", calculateJaroWinklerSimilarity(text1, text2));
        scores.put("semantic", calculateSemanticSimilarity(text1, text2));
        
        // Calculate average score
        double averageScore = scores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        long processingTime = System.currentTimeMillis() - startTime;
        
        SimilarityResponse response = new SimilarityResponse(averageScore, "all", processingTime);
        response.setDetailedScores(scores);
        
        // Create metadata including individual similarity scores
        Map<String, Object> metadata = createMetadata(text1, text2, null);
        metadata.put("cosine_similarity", scores.get("cosine"));
        metadata.put("jaccard_similarity", scores.get("jaccard"));
        metadata.put("levenshtein_similarity", scores.get("levenshtein"));
        metadata.put("jaro_winkler_similarity", scores.get("jaro_winkler"));
        metadata.put("semantic_similarity", scores.get("semantic"));
        response.setMetadata(metadata);
        
        return response;
    }
    
    /**
     * Calculate similarity using a single algorithm
     */
    private double calculateSingleSimilarity(String text1, String text2, String algorithm) {
        return switch (algorithm.toLowerCase()) {
            case "cosine" -> calculateCosineSimilarity(text1, text2);
            case "jaccard" -> calculateJaccardSimilarity(text1, text2);
            case "levenshtein" -> calculateLevenshteinSimilarity(text1, text2);
            case "jaro_winkler" -> calculateJaroWinklerSimilarity(text1, text2);
            case "semantic" -> calculateSemanticSimilarity(text1, text2);
            default -> throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        };
    }
    
    /**
     * Calculate cosine similarity between two texts
     */
    private double calculateCosineSimilarity(String text1, String text2) {
        Map<CharSequence, Integer> vector1 = createWordFrequencyVector(text1);
        Map<CharSequence, Integer> vector2 = createWordFrequencyVector(text2);
        return cosineSimilarity.cosineSimilarity(vector1, vector2);
    }
    
    /**
     * Calculate Jaccard similarity between two texts
     */
    private double calculateJaccardSimilarity(String text1, String text2) {
        Set<String> set1 = createWordSet(text1);
        Set<String> set2 = createWordSet(text2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        if (union.isEmpty()) {
            return 1.0; // Both texts are empty
        }
        
        return (double) intersection.size() / union.size();
    }
    
    /**
     * Calculate Levenshtein similarity (normalized)
     */
    private double calculateLevenshteinSimilarity(String text1, String text2) {
        int distance = levenshteinDistance.apply(text1, text2);
        int maxLength = Math.max(text1.length(), text2.length());
        
        if (maxLength == 0) {
            return 1.0; // Both strings are empty
        }
        
        return 1.0 - ((double) distance / maxLength);
    }
    
    /**
     * Calculate Jaro-Winkler similarity
     */
    private double calculateJaroWinklerSimilarity(String text1, String text2) {
        return jaroWinklerSimilarity.apply(text1, text2);
    }
    
    /**
     * Calculate semantic similarity (basic implementation using word overlap)
     */
    private double calculateSemanticSimilarity(String text1, String text2) {
        // This is a simplified semantic similarity
        // In a production environment, you might want to use word embeddings or pre-trained models
        Set<String> words1 = createWordSet(text1.toLowerCase());
        Set<String> words2 = createWordSet(text2.toLowerCase());
        
        // Remove common stop words for better semantic analysis
        Set<String> stopWords = Set.of("the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should");
        
        words1.removeAll(stopWords);
        words2.removeAll(stopWords);
        
        if (words1.isEmpty() && words2.isEmpty()) {
            return 1.0;
        }
        
        Set<String> union = new HashSet<>(words1);
        union.addAll(words2);
        
        Set<String> intersection = new HashSet<>(words1);
        intersection.retainAll(words2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
    
    /**
     * Create word frequency vector for cosine similarity
     */
    private Map<CharSequence, Integer> createWordFrequencyVector(String text) {
        Map<CharSequence, Integer> vector = new HashMap<>();
        String[] words = text.toLowerCase().split("\\s+");
        
        for (String word : words) {
            word = word.replaceAll("[^a-zA-Z0-9]", "");
            if (!word.isEmpty()) {
                vector.put(word, vector.getOrDefault(word, 0) + 1);
            }
        }
        
        return vector;
    }
    
    /**
     * Create word set for Jaccard similarity
     */
    private Set<String> createWordSet(String text) {
        return Arrays.stream(text.toLowerCase().split("\\s+"))
                .map(word -> word.replaceAll("[^a-zA-Z0-9]", ""))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toSet());
    }
    
    /**
     * Preprocess text based on request parameters
     */
    private String preprocessText(String text, SimilarityRequest request) {
        if (text == null) {
            return "";
        }
        
        String processed = text;
        
        if (request.isNormalizeWhitespace()) {
            processed = processed.replaceAll("\\s+", " ").trim();
        }
        
        if (!request.isCaseSensitive()) {
            processed = processed.toLowerCase();
        }
        
        return processed;
    }
    
    /**
     * Create metadata for the response
     */
    private Map<String, Object> createMetadata(String text1, String text2, SimilarityRequest request) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("text1_length", text1.length());
        metadata.put("text2_length", text2.length());
        metadata.put("text1_word_count", text1.split("\\s+").length);
        metadata.put("text2_word_count", text2.split("\\s+").length);
        
        if (request != null) {
            metadata.put("case_sensitive", request.isCaseSensitive());
            metadata.put("normalize_whitespace", request.isNormalizeWhitespace());
        }
        
        return metadata;
    }
}