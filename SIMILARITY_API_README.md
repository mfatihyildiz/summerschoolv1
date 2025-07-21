# Text Similarity Check AI API

A comprehensive REST API for calculating text similarity using multiple algorithms including Cosine Similarity, Jaccard Similarity, Levenshtein Distance, Jaro-Winkler Similarity, and basic Semantic Similarity.

## Features

- **Multiple Algorithms**: Support for 5 different similarity algorithms
- **Flexible Input**: Accept text via POST (JSON) or GET (query parameters)
- **Configurable Processing**: Case sensitivity and whitespace normalization options
- **Comprehensive Analysis**: Get detailed scores from all algorithms at once
- **Rich Metadata**: Processing time, text statistics, and algorithm information
- **CORS Enabled**: Ready for web application integration
- **Input Validation**: Robust validation with meaningful error messages

## Available Algorithms

### 1. Cosine Similarity (`cosine`)
- **Range**: 0.0 to 1.0
- **Best for**: Document similarity, text classification
- **Description**: Measures the cosine of the angle between two word frequency vectors

### 2. Jaccard Similarity (`jaccard`)
- **Range**: 0.0 to 1.0
- **Best for**: Set similarity, keyword matching
- **Description**: Measures the size of intersection divided by the size of union of word sets

### 3. Levenshtein Distance (`levenshtein`)
- **Range**: 0.0 to 1.0 (normalized)
- **Best for**: Spell checking, fuzzy string matching
- **Description**: Measures the minimum number of single-character edits required to change one string into another

### 4. Jaro-Winkler Similarity (`jaro_winkler`)
- **Range**: 0.0 to 1.0
- **Best for**: Name matching, record linkage
- **Description**: Measures similarity based on character matching and transpositions

### 5. Semantic Similarity (`semantic`)
- **Range**: 0.0 to 1.0
- **Best for**: Content similarity, topic matching
- **Description**: Basic semantic similarity using word overlap (excluding stop words)

### 6. All Algorithms (`all`)
- **Range**: 0.0 to 1.0 (average of all algorithms)
- **Best for**: Comprehensive analysis
- **Description**: Runs all algorithms and returns detailed scores plus an average

## API Endpoints

### Base URL
```
http://localhost:8080/api/similarity
```

### 1. Check Similarity (POST)
**Endpoint**: `POST /api/similarity/check`

**Request Body**:
```json
{
  "text1": "The quick brown fox jumps over the lazy dog",
  "text2": "A fast brown fox leaps over a sleepy dog",
  "algorithm": "cosine",
  "case_sensitive": false,
  "normalize_whitespace": true
}
```

**Response**:
```json
{
  "similarity_score": 0.7745966692414834,
  "algorithm": "cosine",
  "processing_time_ms": 15,
  "timestamp": "2024-01-20T10:30:45.123",
  "success": true,
  "error_message": null,
  "metadata": {
    "text1_length": 43,
    "text2_length": 40,
    "text1_word_count": 9,
    "text2_word_count": 9,
    "case_sensitive": false,
    "normalize_whitespace": true
  }
}
```

### 2. Quick Similarity Check (GET)
**Endpoint**: `GET /api/similarity/quick`

**Parameters**:
- `text1` (required): First text to compare
- `text2` (required): Second text to compare
- `algorithm` (optional): Algorithm to use (default: cosine)

**Example**:
```
GET /api/similarity/quick?text1=Hello%20world&text2=Hello%20earth&algorithm=jaccard
```

### 3. Get All Algorithms (GET)
**Endpoint**: `GET /api/similarity/algorithms`

**Response**:
```json
{
  "algorithms": {
    "cosine": {
      "name": "Cosine Similarity",
      "description": "Measures the cosine of the angle between two word frequency vectors",
      "range": "0.0 to 1.0",
      "best_for": "Document similarity, text classification"
    },
    // ... other algorithms
  },
  "timestamp": "2024-01-20T10:30:45.123",
  "total_algorithms": 6
}
```

### 4. Health Check (GET)
**Endpoint**: `GET /api/similarity/health`

**Response**:
```json
{
  "status": "UP",
  "service": "Text Similarity API",
  "timestamp": "2024-01-20T10:30:45.123",
  "version": "1.0.0"
}
```

### 5. Get Usage Examples (GET)
**Endpoint**: `GET /api/similarity/examples`

**Response**: Complete examples of how to use the API

## Request Parameters

### Required Fields
- `text1`: First text to compare (string)
- `text2`: Second text to compare (string)

### Optional Fields
- `algorithm`: Algorithm to use (default: "cosine")
  - Allowed values: `cosine`, `jaccard`, `levenshtein`, `jaro_winkler`, `semantic`, `all`
- `case_sensitive`: Whether comparison should be case sensitive (default: false)
- `normalize_whitespace`: Whether to normalize whitespace (default: true)

## Usage Examples

### Using cURL

#### Basic similarity check:
```bash
curl -X POST http://localhost:8080/api/similarity/check \
  -H "Content-Type: application/json" \
  -d '{
    "text1": "The quick brown fox",
    "text2": "A fast brown fox",
    "algorithm": "cosine"
  }'
```

#### Get all algorithms:
```bash
curl http://localhost:8080/api/similarity/algorithms
```

#### Quick check with GET:
```bash
curl "http://localhost:8080/api/similarity/quick?text1=hello&text2=hello%20world&algorithm=jaccard"
```

### Using JavaScript (Fetch API)

```javascript
// POST request
const checkSimilarity = async () => {
  const response = await fetch('http://localhost:8080/api/similarity/check', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      text1: 'The quick brown fox jumps over the lazy dog',
      text2: 'A fast brown fox leaps over a sleepy dog',
      algorithm: 'all',
      case_sensitive: false
    })
  });
  
  const result = await response.json();
  console.log(result);
};

// GET request
const quickCheck = async () => {
  const params = new URLSearchParams({
    text1: 'hello world',
    text2: 'hello earth',
    algorithm: 'cosine'
  });
  
  const response = await fetch(`http://localhost:8080/api/similarity/quick?${params}`);
  const result = await response.json();
  console.log(result);
};
```

### Using Python (requests)

```python
import requests

# POST request
def check_similarity():
    url = "http://localhost:8080/api/similarity/check"
    data = {
        "text1": "The quick brown fox jumps over the lazy dog",
        "text2": "A fast brown fox leaps over a sleepy dog",
        "algorithm": "cosine",
        "case_sensitive": False,
        "normalize_whitespace": True
    }
    
    response = requests.post(url, json=data)
    return response.json()

# GET request
def quick_similarity():
    url = "http://localhost:8080/api/similarity/quick"
    params = {
        "text1": "hello world",
        "text2": "hello earth",
        "algorithm": "jaccard"
    }
    
    response = requests.get(url, params=params)
    return response.json()
```

## Running the Application

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Build and Run
```bash
# Build the application
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the JAR file
java -jar target/summerschool-0.0.1-SNAPSHOT.jar
```

The API will be available at `http://localhost:8080`

### Testing
```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=SimilarityApiTest
```

## Error Handling

The API returns appropriate HTTP status codes:

- **200 OK**: Successful similarity calculation
- **400 Bad Request**: Invalid input parameters or validation errors
- **500 Internal Server Error**: Unexpected server errors

Error response format:
```json
{
  "similarity_score": 0.0,
  "algorithm": null,
  "processing_time_ms": 0,
  "timestamp": "2024-01-20T10:30:45.123",
  "success": false,
  "error_message": "Text1 cannot be blank"
}
```

## Performance Considerations

- **Processing Time**: Included in response for performance monitoring
- **Algorithm Choice**: 
  - Cosine and Jaccard are fast for most text sizes
  - Levenshtein can be slower for very long texts
  - "All" algorithms option provides comprehensive analysis but takes longer
- **Text Preprocessing**: Normalize whitespace and case sensitivity options affect performance

## Future Enhancements

- [ ] Support for advanced NLP models (BERT, Word2Vec)
- [ ] Batch processing for multiple text pairs
- [ ] Configurable similarity thresholds
- [ ] Support for different languages
- [ ] Caching for frequently compared texts
- [ ] Rate limiting and API key authentication
- [ ] More sophisticated semantic similarity using word embeddings

## API Dependencies

- Spring Boot 3.3.5
- Apache Commons Text 1.11.0
- Stanford CoreNLP 4.5.4 (for future semantic enhancements)
- Jackson for JSON processing
- Spring Boot Validation

## License

This API is part of the Summer School application and follows the same licensing terms.