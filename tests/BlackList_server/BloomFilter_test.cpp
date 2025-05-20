#include <gtest/gtest.h>
#include "../../BlackList_server/include/BloomFilter.h"
#include "../../BlackList_server/include/MultiHash.h"
#include <vector>
#include <string>
#include <fstream>
#include <filesystem>

// -----------------------------
// Test suite: BloomFilterTest
// -----------------------------

class BloomFilterTest : public ::testing::Test {
protected:
    void SetUp() override {
        // Create a test directory if it doesn't exist
        if (!std::filesystem::exists("test_data")) {
            std::filesystem::create_directory("test_data");
        }
    }

    void TearDown() override {
        // Clean up test files
        if (std::filesystem::exists("test_data/bloom_test.dat")) {
            std::filesystem::remove("test_data/bloom_test.dat");
        }
    }

    // Helper function to create a vector of hash functions
    std::vector<HashFunc*> createHashFunctions(const std::vector<int>& hashCounts) {
        std::vector<HashFunc*> functions;
        for (int count : hashCounts) {
            functions.push_back(new MultiHash(count));
        }
        return functions;
    }
};

// ----------------------------
// Test case: AddURL
// ----------------------------

TEST_F(BloomFilterTest, AddURL) {
    // Create a bloom filter with a single hash function
    std::vector<HashFunc*> hashFuncs = createHashFunctions({1});
    BloomFilter filter(8, hashFuncs);
    
    // Add a URL
    filter.add("www.example.com");
    
    // Check if the URL is possibly contained
    EXPECT_TRUE(filter.possiblyContain("www.example.com"));
}

// ----------------------------
// Test case: CheckNonExistentURL
// ----------------------------

TEST_F(BloomFilterTest, CheckNonExistentURL) {
    // Create a bloom filter with two hash functions
    std::vector<HashFunc*> hashFuncs = createHashFunctions({1, 2});
    BloomFilter filter(16, hashFuncs);
    
    // Add a different URL
    filter.add("www.example.com");
    
    // Check if a different URL is not contained
    EXPECT_FALSE(filter.possiblyContain("www.different.com"));
}

// ----------------------------
// Test case: SaveAndLoad
// ----------------------------

TEST_F(BloomFilterTest, SaveAndLoad) {
    // Create a bloom filter
    std::vector<HashFunc*> hashFuncs = createHashFunctions({1});
    BloomFilter filter(8, hashFuncs);
    
    // Add some URLs
    filter.add("www.example1.com");
    filter.add("www.example2.com");
    
    // Save the filter to a file
    filter.saveToFile("test_data/bloom_test.dat");
    
    // Create a new filter with the same hash functions
    std::vector<HashFunc*> newHashFuncs = createHashFunctions({1});
    BloomFilter newFilter(8, newHashFuncs);
    
    // Load the filter from the file
    newFilter.loadFromFile("test_data/bloom_test.dat");
    
    // Check if the new filter contains the same URLs
    EXPECT_TRUE(newFilter.possiblyContain("www.example1.com"));
    EXPECT_TRUE(newFilter.possiblyContain("www.example2.com"));
}

// ----------------------------
// Test case: FalsePositives
// ----------------------------

TEST_F(BloomFilterTest, FalsePositives) {
    // Create a small bloom filter to increase chance of false positives
    std::vector<HashFunc*> hashFuncs = createHashFunctions({1});
    BloomFilter filter(4, hashFuncs);
    
    // Add multiple URLs to increase bit set density
    filter.add("www.example1.com");
    filter.add("www.example2.com");
    filter.add("www.example3.com");
    
    // Test multiple URLs and count false positives
    int falsePositives = 0;
    int totalTests = 20;
    
    for (int i = 10; i < 10 + totalTests; ++i) {
        std::string testUrl = "www.test" + std::to_string(i) + ".com";
        if (filter.possiblyContain(testUrl)) {
            falsePositives++;
        }
    }
    
    // We expect some false positives but not for all URLs
    // This is a probabilistic test, so we're just checking if falsePositives > 0 and < totalTests
    EXPECT_GT(falsePositives, 0);
    EXPECT_LT(falsePositives, totalTests);
}

// ----------------------------
// Test case: MultipleHashFunctions
// ----------------------------

TEST_F(BloomFilterTest, MultipleHashFunctions) {
    // Create a bloom filter with three hash functions
    std::vector<HashFunc*> hashFuncs = createHashFunctions({1, 2, 3});
    BloomFilter filter(32, hashFuncs);
    
    // Add a URL
    filter.add("www.example.com");
    
    // Check if the URL is contained
    EXPECT_TRUE(filter.possiblyContain("www.example.com"));
    
    // Check if a different URL is not contained
    EXPECT_FALSE(filter.possiblyContain("www.different.com"));
}

// ----------------------------
// Test case: Getters
// ----------------------------

TEST_F(BloomFilterTest, Getters) {
    // Create a bloom filter
    std::vector<HashFunc*> hashFuncs = createHashFunctions({1, 2});
    int size = 16;
    BloomFilter filter(size, hashFuncs);
    
    // Test size and hash function count getters
    EXPECT_EQ(filter.getSize(), size);
    EXPECT_EQ(filter.getHashNum(), 2);
    
    // Test bit array getter
    const std::vector<bool>& bitArray = filter.getBitArray();
    EXPECT_EQ(bitArray.size(), size);
    
    // Test hash function getter
    HashFunc* hashFunc = filter.getHashFunction(0);
    EXPECT_NE(hashFunc, nullptr);
    EXPECT_EQ(filter.getHashFunction(-1), nullptr); // Invalid index
    EXPECT_EQ(filter.getHashFunction(2), nullptr);  // Invalid index
}
