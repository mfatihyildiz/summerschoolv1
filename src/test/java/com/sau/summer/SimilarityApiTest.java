package com.sau.summer;

import com.sau.summer.dto.SimilarityRequest;
import com.sau.summer.dto.SimilarityResponse;
import com.sau.summer.service.SimilarityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SimilarityApiTest {
    
    @Autowired
    private SimilarityService similarityService;
    
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
        assertTrue(response.getSimilarityScore() >= 0.0);
        assertTrue(response.getSimilarityScore() <= 1.0);
        assertTrue(response.getSimilarityScore() > 0.5); // Should be similar
    }
    
    @Test
    public void testJaccardSimilarity() {
        SimilarityRequest request = new SimilarityRequest(
            "hello world",
            "hello earth",
            "jaccard"
        );
        
        SimilarityResponse response = similarityService.calculateSimilarity(request);
        
        assertTrue(response.isSuccess());
        assertEquals("jaccard", response.getAlgorithm());
        assertTrue(response.getSimilarityScore() >= 0.0);
        assertTrue(response.getSimilarityScore() <= 1.0);
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
    public void testJaroWinklerSimilarity() {
        SimilarityRequest request = new SimilarityRequest(
            "martha",
            "marhta",
            "jaro_winkler"
        );
        
        SimilarityResponse response = similarityService.calculateSimilarity(request);
        
        assertTrue(response.isSuccess());
        assertEquals("jaro_winkler", response.getAlgorithm());
        assertTrue(response.getSimilarityScore() >= 0.0);
        assertTrue(response.getSimilarityScore() <= 1.0);
    }
    
    @Test
    public void testSemanticSimilarity() {
        SimilarityRequest request = new SimilarityRequest(
            "car automobile vehicle",
            "vehicle car transportation",
            "semantic"
        );
        
        SimilarityResponse response = similarityService.calculateSimilarity(request);
        
        assertTrue(response.isSuccess());
        assertEquals("semantic", response.getAlgorithm());
        assertTrue(response.getSimilarityScore() >= 0.0);
        assertTrue(response.getSimilarityScore() <= 1.0);
    }
    
    @Test
    public void testAllAlgorithms() {
        SimilarityRequest request = new SimilarityRequest(
            "This is a test sentence",
            "This is a test phrase",
            "all"
        );
        
        SimilarityResponse response = similarityService.calculateSimilarity(request);
        
        assertTrue(response.isSuccess());
        assertEquals("all", response.getAlgorithm());
        assertNotNull(response.getDetailedScores());
        assertTrue(response.getDetailedScores().size() >= 5);
        assertTrue(response.getDetailedScores().containsKey("cosine"));
        assertTrue(response.getDetailedScores().containsKey("jaccard"));
        assertTrue(response.getDetailedScores().containsKey("levenshtein"));
        assertTrue(response.getDetailedScores().containsKey("jaro_winkler"));
        assertTrue(response.getDetailedScores().containsKey("semantic"));
    }
    
    @Test
    public void testIdenticalTexts() {
        SimilarityRequest request = new SimilarityRequest(
            "identical text",
            "identical text",
            "cosine"
        );
        
        SimilarityResponse response = similarityService.calculateSimilarity(request);
        
        assertTrue(response.isSuccess());
        assertEquals(1.0, response.getSimilarityScore(), 0.001);
    }
    
    @Test
    public void testEmptyTexts() {
        SimilarityRequest request = new SimilarityRequest(
            "",
            "",
            "cosine"
        );
        
        SimilarityResponse response = similarityService.calculateSimilarity(request);
        
        assertTrue(response.isSuccess());
        // Empty texts should be considered identical
        assertTrue(response.getSimilarityScore() >= 0.0);
    }
    
    @Test
    public void testCaseSensitivity() {
        SimilarityRequest request1 = new SimilarityRequest(
            "Hello World",
            "hello world",
            "cosine"
        );
        request1.setCaseSensitive(false);
        
        SimilarityRequest request2 = new SimilarityRequest(
            "Hello World",
            "hello world",
            "cosine"
        );
        request2.setCaseSensitive(true);
        
        SimilarityResponse response1 = similarityService.calculateSimilarity(request1);
        SimilarityResponse response2 = similarityService.calculateSimilarity(request2);
        
        assertTrue(response1.isSuccess());
        assertTrue(response2.isSuccess());
        
        // Case insensitive should have higher similarity
        assertTrue(response1.getSimilarityScore() >= response2.getSimilarityScore());
    }
    
    @Test
    public void testMetadata() {
        SimilarityRequest request = new SimilarityRequest(
            "Hello world test",
            "Test hello universe",
            "cosine"
        );
        
        SimilarityResponse response = similarityService.calculateSimilarity(request);
        
        assertTrue(response.isSuccess());
        assertNotNull(response.getMetadata());
        assertTrue(response.getMetadata().containsKey("text1_length"));
        assertTrue(response.getMetadata().containsKey("text2_length"));
        assertTrue(response.getMetadata().containsKey("text1_word_count"));
        assertTrue(response.getMetadata().containsKey("text2_word_count"));
    }
}