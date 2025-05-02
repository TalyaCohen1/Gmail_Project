#include <gtest/gtest.h>
#include "../src/PostCommand.h"
#include "../src/GetCommand.h"
#include "../src/BloomFilter.h"
#include "../src/URLBlacklist.h"
#include "../src/MultiHash.h"
#include <vector>
#include <string>
#include <sstream>

// -------------------------------
// Test suite: CommandTest
// -------------------------------
class CommandTest : public ::testing::Test {
protected:
    // SetUp runs before each test in the suite
    void SetUp() override {
        // Create hash functions for our test Bloom filter
        hashFuncs.push_back(new MultiHash(1));
        
        // Create a test Bloom filter with a size of 8 and the hash functions
        bloomFilter = new BloomFilter(8, hashFuncs);
        
        // Create a test URLBlacklist
        blacklist = new URLBlacklist();
    }

    // TearDown runs after each test in the suite
    void TearDown() override {
        // Clean up resources: delete Bloom filter and URL blacklist
        delete bloomFilter;
        delete blacklist;
        
        // Clean up hash functions
        for (auto* hashFunc : hashFuncs) {
            delete hashFunc;
        }
        hashFuncs.clear();
    }
    
    // Redirect cout to capture output for testing
    void RedirectCoutToBuffer() {
        oldCoutBuffer = std::cout.rdbuf();
        std::cout.rdbuf(outputBuffer.rdbuf());
    }
    
    // Restore cout after capturing output
    void RestoreCout() {
        std::cout.rdbuf(oldCoutBuffer);
    }

    std::vector<HashFunc*> hashFuncs;     // Holds the hash functions for Bloom filter
    BloomFilter* bloomFilter;             // The Bloom filter being tested
    URLBlacklist* blacklist;              // The URL blacklist being tested
    std::stringstream outputBuffer;       // Buffer to capture std::cout output
    std::streambuf* oldCoutBuffer;       // Holds the original cout buffer
};

// -------------------------------
// Test: AddCommand
// -------------------------------
TEST_F(CommandTest, GetCommand) {
    // Create an AddCommand instance with Bloom filter and URL blacklist
    GetCommand addCmd(*bloomFilter, *blacklist);
    
    // Execute the AddCommand to add a URL
    addCmd.execute("www.example.com");
    
    // Verify the URL was added to both Bloom filter and URL blacklist
    EXPECT_TRUE(bloomFilter->possiblyContain("www.example.com"));
    EXPECT_TRUE(blacklist->contains("www.example.com"));
}

// -------------------------------
// Test: CheckCommand with non-blacklisted URL
// -------------------------------
TEST_F(CommandTest, CheckCommandNonBlacklisted) {
    // Redirect cout to capture output
    RedirectCoutToBuffer();
    
    // Create a CheckCommand instance with Bloom filter and blacklist
    CheckCommand checkCmd(*bloomFilter, *blacklist);
    
    // Execute CheckCommand for a URL that's not in the blacklist
    checkCmd.execute("www.notblacklisted.com");
    
    // Restore the original cout buffer
    RestoreCout();
    
    // Verify that the output is "false\n" indicating the URL is not blacklisted
    EXPECT_EQ(outputBuffer.str(), "200 Ok\n\nfalse\n");
}

// -------------------------------
// Test: CheckCommand with blacklisted URL
// -------------------------------
TEST_F(CommandTest, CheckCommandBlacklisted) {
    // Add a URL to both Bloom filter and URL blacklist
    bloomFilter->add("www.blacklisted.com");
    blacklist->add("www.blacklisted.com");
    
    // Redirect cout to capture output
    RedirectCoutToBuffer();
    
    // Create a CheckCommand instance with Bloom filter and blacklist
    GetCommand checkCmd(*bloomFilter, *blacklist);
    
    // Execute CheckCommand for the blacklisted URL
    checkCmd.execute("www.blacklisted.com");
    
    // Restore the original cout buffer
    RestoreCout();
    
    // Verify the output is "true true\n" indicating the URL is blacklisted
    EXPECT_EQ(outputBuffer.str(), "200 Ok\n\ntrue true\n");
}

// -------------------------------
// Test: CheckCommand with false positive (Bloom filter)
//
// Purpose: To test if the Bloom filter correctly identifies false positives.
// -------------------------------
TEST_F(CommandTest, CheckCommandFalsePositive) {
    // Create a small Bloom filter with a chance for false positives
    std::vector<HashFunc*> smallHashFuncs;
    smallHashFuncs.push_back(new MultiHash(1));
    BloomFilter smallFilter(4, smallHashFuncs);
    
    // Add multiple URLs to the Bloom filter to increase the chance of false positives
    smallFilter.add("www.example1.com");
    smallFilter.add("www.example2.com");
    smallFilter.add("www.example3.com");
    
    // Try to find a false positive URL
    std::string falsePositiveUrl = "";
    for (int i = 10; i < 100; ++i) {
        std::string testUrl = "www.test" + std::to_string(i) + ".com";
        if (smallFilter.possiblyContain(testUrl)) {
            falsePositiveUrl = testUrl;
            break;
        }
    }
    
    // If a false positive URL is found
    if (!falsePositiveUrl.empty()) {
        // Redirect cout to capture output
        RedirectCoutToBuffer();
        
        // Create a CheckCommand with the small Bloom filter
        GetCommand checkCmd(smallFilter, *blacklist);
        
        // Execute CheckCommand for the false positive URL
        checkCmd.execute("2 " + falsePositiveUrl);
        
        // Restore the original cout buffer
        RestoreCout();
        
        // Verify the output is "true false\n" because the Bloom filter returns true (false positive)
        // and the blacklist returns false
        EXPECT_EQ(outputBuffer.str(), "200 Ok\n\ntrue false\n");
    }
    
    // Clean up hash functions
    for (auto* hashFunc : smallHashFuncs) {
        delete hashFunc;
    }
}
