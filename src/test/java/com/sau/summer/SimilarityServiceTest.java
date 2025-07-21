package com.sau.summer;

import com.sau.summer.dto.SimilarityRequest;
import com.sau.summer.dto.SimilarityResponse;
import com.sau.summer.service.SimilarityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SimilarityService (not requiring Spring context)
 */
public class SimilarityServiceTest {
    
    private SimilarityService similarityService;
    
    @BeforeEach
    public void setUp() {
        similarityService = new SimilarityService();
    }
    
    @Test
    public void testCosineSimilarity() {
        SimilarityRequest request = new SimilarityRequest(
            "The quick brown fox jumps over the lazy dog",
            "A fast brown fox leaps over a sleepy dog",
            "cosine"
        );
        
        SimilarityResponse response = similarityService.calculateSimilarity(request);
        
        assertTrue(response.isSuccess());
        assertEquals("cosine", response.getAlgorithm());
        assertTrue(response.getSimilarityScore() > 0.0); // Should be greater than 0
        assertTrue(response.getSimilarityScore() <= 1.0);
    }
    
    @Test
    public void testIdenticalTexts() {
        SimilarityRequest request = new SimilarityRequest(
            "Hello World",
            "Hello World",
            "cosine"
        );
        
        SimilarityResponse response = similarityService.calculateSimilarity(request);
        
        assertTrue(response.isSuccess());
        assertEquals(1.0, response.getSimilarityScore(), 0.001); // Should be exactly 1.0
    }
    
    @Test
    public void testJaccardSimilarity() {
        SimilarityRequest request = new SimilarityRequest(
            "apple banana cherry",
            "banana cherry date",
            "jaccard"
        );
        
        SimilarityResponse response = similarityService.calculateSimilarity(request);
        
        assertTrue(response.isSuccess());
        assertEquals("jaccard", response.getAlgorithm());
        assertTrue(response.getSimilarityScore() > 0.0);
        assertTrue(response.getSimilarityScore() < 1.0);
    }
    
    @Test
    public void testLevenshteinSimilarity() {
        SimilarityRequest request = new SimilarityRequest(
            "kitten",
            "sitting",
            "levenshtein"
        );
        
        SimilarityResponse response = similarityService.calculateSimilarity(request);
        
        assertTrue(response.isSuccess());
        assertEquals("levenshtein", response.getAlgorithm());
        assertTrue(response.getSimilarityScore() >= 0.0);
        assertTrue(response.getSimilarityScore() <= 1.0);
    }
    
    @Test
    public void testAllAlgorithms() {
        SimilarityRequest request = new SimilarityRequest(
            "Machine learning is amazing",
            "AI and machine learning are incredible",
            "all"
        );
        
        SimilarityResponse response = similarityService.calculateSimilarity(request);
        
        assertTrue(response.isSuccess());
        assertEquals("all", response.getAlgorithm());
        assertNotNull(response.getMetadata());
        assertTrue(response.getMetadata().containsKey("cosine_similarity"));
        assertTrue(response.getMetadata().containsKey("jaccard_similarity"));
        assertTrue(response.getMetadata().containsKey("levenshtein_similarity"));
        assertTrue(response.getMetadata().containsKey("jaro_winkler_similarity"));
    }
    
    @Test
    public void testEmptyTexts() {
        SimilarityRequest request = new SimilarityRequest(
            "",
            "some text",
            "cosine"
        );
        
        SimilarityResponse response = similarityService.calculateSimilarity(request);
        
        assertTrue(response.isSuccess());
        assertEquals(0.0, response.getSimilarityScore());
    }
}