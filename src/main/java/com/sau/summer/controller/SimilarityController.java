package com.sau.summer.controller;

import com.sau.summer.dto.SimilarityRequest;
import com.sau.summer.dto.SimilarityResponse;
import com.sau.summer.service.SimilarityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * REST Controller for Text Similarity API
 */
@RestController
@RequestMapping("/api/similarity")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SimilarityController {
    
    private final SimilarityService similarityService;
    
    @Autowired
    public SimilarityController(SimilarityService similarityService) {
        this.similarityService = similarityService;
    }
    
    /**
     * Calculate similarity between two texts
     * 
     * @param request The similarity request containing text1, text2, and algorithm
     * @return SimilarityResponse with the calculated similarity score
     */
    @PostMapping("/check")
    public ResponseEntity<SimilarityResponse> checkSimilarity(@Valid @RequestBody SimilarityRequest request) {
        try {
            SimilarityResponse response = similarityService.calculateSimilarity(request);
            
            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(SimilarityResponse.error("Invalid request: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimilarityResponse.error("Internal server error: " + e.getMessage()));
        }
    }
    
    /**
     * Quick similarity check using GET request
     * 
     * @param text1 First text to compare
     * @param text2 Second text to compare
     * @param algorithm Algorithm to use (optional, defaults to cosine)
     * @return SimilarityResponse with the calculated similarity score
     */
    @GetMapping("/quick")
    public ResponseEntity<SimilarityResponse> quickSimilarityCheck(
            @RequestParam("text1") String text1,
            @RequestParam("text2") String text2,
            @RequestParam(value = "algorithm", defaultValue = "cosine") String algorithm) {
        
        try {
            SimilarityRequest request = new SimilarityRequest(text1, text2, algorithm);
            SimilarityResponse response = similarityService.calculateSimilarity(request);
            
            if (!response.isSuccess()) {
                return ResponseEntity.badRequest().body(response);
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SimilarityResponse.error("Internal server error: " + e.getMessage()));
        }
    }
    
    /**
     * Get information about available algorithms
     * 
     * @return Map containing algorithm information
     */
    @GetMapping("/algorithms")
    public ResponseEntity<Map<String, Object>> getAlgorithmInfo() {
        Map<String, Object> algorithms = new HashMap<>();
        
        Map<String, Object> cosine = new HashMap<>();
        cosine.put("name", "Cosine Similarity");
        cosine.put("description", "Measures the cosine of the angle between two word frequency vectors");
        cosine.put("range", "0.0 to 1.0");
        cosine.put("best_for", "Document similarity, text classification");
        algorithms.put("cosine", cosine);
        
        Map<String, Object> jaccard = new HashMap<>();
        jaccard.put("name", "Jaccard Similarity");
        jaccard.put("description", "Measures the size of intersection divided by the size of union of word sets");
        jaccard.put("range", "0.0 to 1.0");
        jaccard.put("best_for", "Set similarity, keyword matching");
        algorithms.put("jaccard", jaccard);
        
        Map<String, Object> levenshtein = new HashMap<>();
        levenshtein.put("name", "Levenshtein Distance (Normalized)");
        levenshtein.put("description", "Measures the minimum number of single-character edits required to change one string into another");
        levenshtein.put("range", "0.0 to 1.0");
        levenshtein.put("best_for", "Spell checking, fuzzy string matching");
        algorithms.put("levenshtein", levenshtein);
        
        Map<String, Object> jaroWinkler = new HashMap<>();
        jaroWinkler.put("name", "Jaro-Winkler Similarity");
        jaroWinkler.put("description", "Measures similarity based on character matching and transpositions");
        jaroWinkler.put("range", "0.0 to 1.0");
        jaroWinkler.put("best_for", "Name matching, record linkage");
        algorithms.put("jaro_winkler", jaroWinkler);
        
        Map<String, Object> semantic = new HashMap<>();
        semantic.put("name", "Semantic Similarity");
        semantic.put("description", "Basic semantic similarity using word overlap (excluding stop words)");
        semantic.put("range", "0.0 to 1.0");
        semantic.put("best_for", "Content similarity, topic matching");
        algorithms.put("semantic", semantic);
        
        Map<String, Object> all = new HashMap<>();
        all.put("name", "All Algorithms");
        all.put("description", "Runs all algorithms and returns detailed scores plus an average");
        all.put("range", "0.0 to 1.0");
        all.put("best_for", "Comprehensive analysis");
        algorithms.put("all", all);
        
        Map<String, Object> response = new HashMap<>();
        response.put("algorithms", algorithms);
        response.put("timestamp", LocalDateTime.now());
        response.put("total_algorithms", algorithms.size());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check endpoint
     * 
     * @return Simple health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Text Similarity API");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }
    
    /**
     * Get usage examples
     * 
     * @return Examples of how to use the API
     */
    @GetMapping("/examples")
    public ResponseEntity<Map<String, Object>> getExamples() {
        Map<String, Object> examples = new HashMap<>();
        
        // POST request example
        Map<String, Object> postExample = new HashMap<>();
        postExample.put("method", "POST");
        postExample.put("endpoint", "/api/similarity/check");
        postExample.put("headers", Map.of("Content-Type", "application/json"));
        
        Map<String, Object> postBody = new HashMap<>();
        postBody.put("text1", "The quick brown fox jumps over the lazy dog");
        postBody.put("text2", "A fast brown fox leaps over a sleepy dog");
        postBody.put("algorithm", "cosine");
        postBody.put("case_sensitive", false);
        postBody.put("normalize_whitespace", true);
        postExample.put("body", postBody);
        
        // GET request example
        Map<String, Object> getExample = new HashMap<>();
        getExample.put("method", "GET");
        getExample.put("endpoint", "/api/similarity/quick");
        getExample.put("parameters", Map.of(
                "text1", "Hello world",
                "text2", "Hello earth", 
                "algorithm", "jaccard"
        ));
        
        examples.put("post_request", postExample);
        examples.put("get_request", getExample);
        examples.put("algorithm_info", "/api/similarity/algorithms");
        examples.put("health_check", "/api/similarity/health");
        
        return ResponseEntity.ok(examples);
    }
}