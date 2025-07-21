#!/usr/bin/env python3
"""
Demo script for the Text Similarity AI API
This script demonstrates the functionality of the similarity API with various text examples.
"""

import requests
import json
import time

# API base URL - change this to match your server
API_BASE_URL = "http://localhost:8090/api/similarity"

def test_similarity(text1, text2, algorithm="cosine", description=""):
    """
    Test similarity between two texts using the specified algorithm
    """
    print(f"\n{'='*60}")
    print(f"TEST: {description}")
    print(f"{'='*60}")
    print(f"Text 1: {text1}")
    print(f"Text 2: {text2}")
    print(f"Algorithm: {algorithm}")
    print("-" * 60)
    
    payload = {
        "text1": text1,
        "text2": text2,
        "algorithm": algorithm,
        "case_sensitive": False,
        "normalize_whitespace": True
    }
    
    try:
        response = requests.post(f"{API_BASE_URL}/check", json=payload)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ SUCCESS: Similarity Score = {result['similarity_score']:.4f}")
            print(f"   Algorithm: {result['algorithm']}")
            print(f"   Processing Time: {result['processing_time_ms']}ms")
            
            if 'metadata' in result and result['metadata']:
                print(f"   Text 1 Length: {result['metadata'].get('text1_length', 'N/A')}")
                print(f"   Text 2 Length: {result['metadata'].get('text2_length', 'N/A')}")
                
                if algorithm == "all":
                    print("   Individual Scores:")
                    for key, value in result['metadata'].items():
                        if key.endswith('_similarity'):
                            print(f"     {key.replace('_', ' ').title()}: {value:.4f}")
            
        else:
            print(f"❌ ERROR: HTTP {response.status_code}")
            print(f"   Response: {response.text}")
            
    except requests.exceptions.ConnectionError:
        print("❌ ERROR: Could not connect to the API. Make sure the server is running on port 8090.")
    except Exception as e:
        print(f"❌ ERROR: {str(e)}")

def get_algorithm_info():
    """
    Get information about available algorithms
    """
    print("\n" + "="*60)
    print("GETTING ALGORITHM INFORMATION")
    print("="*60)
    
    try:
        response = requests.get(f"{API_BASE_URL}/algorithms")
        
        if response.status_code == 200:
            result = response.json()
            print("✅ Available Algorithms:")
            for algo in result.get('algorithms', []):
                print(f"   • {algo}")
            print(f"   Default: {result.get('default_algorithm', 'N/A')}")
        else:
            print(f"❌ ERROR: HTTP {response.status_code}")
            
    except requests.exceptions.ConnectionError:
        print("❌ ERROR: Could not connect to the API")
    except Exception as e:
        print(f"❌ ERROR: {str(e)}")

def main():
    """
    Run all similarity tests
    """
    print("🚀 Text Similarity AI API Demo")
    print("Author: AI Assistant")
    print(f"Testing API at: {API_BASE_URL}")
    
    # Get algorithm information
    get_algorithm_info()
    
    # Test 1: Identical texts
    test_similarity(
        "Hello World",
        "Hello World",
        "cosine",
        "Identical Texts (Should be ~1.0)"
    )
    
    # Test 2: Similar texts
    test_similarity(
        "The quick brown fox jumps over the lazy dog",
        "A fast brown fox leaps over a sleepy dog",
        "cosine",
        "Similar Texts with Different Words"
    )
    
    # Test 3: Very different texts
    test_similarity(
        "Machine learning and artificial intelligence",
        "Cooking recipes and kitchen utensils",
        "cosine",
        "Completely Different Topics"
    )
    
    # Test 4: Jaccard similarity
    test_similarity(
        "apple banana cherry date",
        "banana cherry date elderberry",
        "jaccard",
        "Word Set Overlap (Jaccard)"
    )
    
    # Test 5: Levenshtein distance
    test_similarity(
        "kitten",
        "sitting",
        "levenshtein",
        "Character Edit Distance (Levenshtein)"
    )
    
    # Test 6: Jaro-Winkler similarity
    test_similarity(
        "DWAYNE",
        "DUANE",
        "jaro_winkler",
        "Name Similarity (Jaro-Winkler)"
    )
    
    # Test 7: All algorithms at once
    test_similarity(
        "Machine learning is revolutionizing technology",
        "AI and machine learning are transforming tech",
        "all",
        "All Algorithms Comparison"
    )
    
    # Test 8: Empty text handling
    test_similarity(
        "",
        "Some text here",
        "cosine",
        "Empty Text Handling"
    )
    
    # Test 9: Case sensitivity
    test_similarity(
        "Hello WORLD",
        "hello world",
        "cosine",
        "Case Insensitive Comparison"
    )
    
    print(f"\n{'='*60}")
    print("✅ DEMO COMPLETE")
    print("="*60)
    print("\n📝 Notes:")
    print("   • Similarity scores range from 0.0 (no similarity) to 1.0 (identical)")
    print("   • Each algorithm has different strengths for different text types")
    print("   • The API supports both POST /check and GET /quick endpoints")
    print("   • All algorithms can be compared at once using algorithm='all'")
    print("\n🔗 API Documentation: See SIMILARITY_API_README.md")

if __name__ == "__main__":
    main()