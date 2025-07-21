package com.sau.summer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for similarity check responses
 */
public class SimilarityResponse {
    
    @JsonProperty("similarity_score")
    private double similarityScore;
    
    @JsonProperty("algorithm")
    private String algorithm;
    
    @JsonProperty("processing_time_ms")
    private long processingTimeMs;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("error_message")
    private String errorMessage;
    
    @JsonProperty("metadata")
    private Map<String, Object> metadata;
    
    @JsonProperty("detailed_scores")
    private Map<String, Double> detailedScores;
    
    // Constructors
    public SimilarityResponse() {
        this.timestamp = LocalDateTime.now();
        this.success = true;
    }
    
    public SimilarityResponse(double similarityScore, String algorithm, long processingTimeMs) {
        this();
        this.similarityScore = similarityScore;
        this.algorithm = algorithm;
        this.processingTimeMs = processingTimeMs;
    }
    
    public static SimilarityResponse error(String errorMessage) {
        SimilarityResponse response = new SimilarityResponse();
        response.success = false;
        response.errorMessage = errorMessage;
        return response;
    }
    
    // Getters and Setters
    public double getSimilarityScore() {
        return similarityScore;
    }
    
    public void setSimilarityScore(double similarityScore) {
        this.similarityScore = similarityScore;
    }
    
    public String getAlgorithm() {
        return algorithm;
    }
    
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    public long getProcessingTimeMs() {
        return processingTimeMs;
    }
    
    public void setProcessingTimeMs(long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public Map<String, Double> getDetailedScores() {
        return detailedScores;
    }
    
    public void setDetailedScores(Map<String, Double> detailedScores) {
        this.detailedScores = detailedScores;
    }
    
    @Override
    public String toString() {
        return "SimilarityResponse{" +
                "similarityScore=" + similarityScore +
                ", algorithm='" + algorithm + '\'' +
                ", processingTimeMs=" + processingTimeMs +
                ", timestamp=" + timestamp +
                ", success=" + success +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}