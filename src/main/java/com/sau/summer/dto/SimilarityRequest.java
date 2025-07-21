package com.sau.summer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO for similarity check requests
 */
public class SimilarityRequest {
    
    @JsonProperty("text1")
    @NotBlank(message = "Text1 cannot be blank")
    private String text1;
    
    @JsonProperty("text2")
    @NotBlank(message = "Text2 cannot be blank")
    private String text2;
    
    @JsonProperty("algorithm")
    @Pattern(regexp = "^(cosine|jaccard|levenshtein|jaro_winkler|semantic|all)$", 
             message = "Algorithm must be one of: cosine, jaccard, levenshtein, jaro_winkler, semantic, all")
    private String algorithm = "cosine";
    
    @JsonProperty("case_sensitive")
    private boolean caseSensitive = false;
    
    @JsonProperty("normalize_whitespace")
    private boolean normalizeWhitespace = true;
    
    // Constructors
    public SimilarityRequest() {}
    
    public SimilarityRequest(String text1, String text2, String algorithm) {
        this.text1 = text1;
        this.text2 = text2;
        this.algorithm = algorithm;
    }
    
    // Getters and Setters
    public String getText1() {
        return text1;
    }
    
    public void setText1(String text1) {
        this.text1 = text1;
    }
    
    public String getText2() {
        return text2;
    }
    
    public void setText2(String text2) {
        this.text2 = text2;
    }
    
    public String getAlgorithm() {
        return algorithm;
    }
    
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
    
    public boolean isNormalizeWhitespace() {
        return normalizeWhitespace;
    }
    
    public void setNormalizeWhitespace(boolean normalizeWhitespace) {
        this.normalizeWhitespace = normalizeWhitespace;
    }
    
    @Override
    public String toString() {
        return "SimilarityRequest{" +
                "text1='" + text1 + '\'' +
                ", text2='" + text2 + '\'' +
                ", algorithm='" + algorithm + '\'' +
                ", caseSensitive=" + caseSensitive +
                ", normalizeWhitespace=" + normalizeWhitespace +
                '}';
    }
}