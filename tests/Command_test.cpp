#include <gtest/gtest.h>
#include "../src/AddCommand.h"
#include "../src/CheckCommand.h"
#include "../src/BloomFilter.h"
#include "../src/URLBlacklist.h"
#include "../src/PersistentManager.h"
#include "../src/MultiHash.h"
#include <vector>
#include <string>
#include <filesystem>
#include <fstream>
#include <sstream>


class CommandTest : public ::testing::Test {
protected:
    void SetUp() override {
        // Create a test directory if it doesn't exist
        if (!std::filesystem::exists("test_data")) {
            std::filesystem::create_directory("test_data");
        }
        
        // Create hash functions for our test bloom filter
        hashFuncs.push_back(new MultiHash(1));
        
        // Create a test bloom filter
        bloomFilter = new BloomFilter(8, hashFuncs);
        
        // Create a test URL blacklist
        blacklist = new URLBlacklist();
        
        // Create a persistent manager
        persistentManager = new PersistentManager("test_data");
    }

    void TearDown() override {
        // Clean up resources
        delete bloomFilter;
        delete blacklist;
        delete persistentManager;
        
        // Clean up test files
        if (std::filesystem::exists("test_data/blacklist.txt")) {
            std::filesystem::remove("test_data/blacklist.txt");
        }
    }
    
    // Redirect cout for testing output
    void RedirectCoutToBuffer() {
        oldCoutBuffer = std::cout.rdbuf();
        std::cout.rdbuf(outputBuffer.rdbuf());
    }
    
    // Restore cout
    void RestoreCout() {
        std::cout.rdbuf(oldCoutBuffer);
    }

    std::vector<HashFunc*> hashFuncs;
    BloomFilter* bloomFilter;
    URLBlacklist* blacklist;
    PersistentManager* persistentManager;
    std::stringstream outputBuffer;
    std::streambuf* oldCoutBuffer;
};

// Test AddCommand
TEST_F(CommandTest, AddCommand) {
    // Create an AddCommand
    AddCommand addCmd(*bloomFilter, *blacklist, *persistentManager);
    
    // Execute the command
    addCmd.execute("1 www.example.com");
    
    // Verify the URL was added to both the bloom filter and blacklist
    EXPECT_TRUE(bloomFilter->possiblyContain("www.example.com"));
    EXPECT_TRUE(blacklist->contains("www.example.com"));
    
    // Verify a file was created
    EXPECT_TRUE(std::filesystem::exists("test_data/blacklist.txt"));
}

// Test CheckCommand with non-blacklisted URL
TEST_F(CommandTest, CheckCommandNonBlacklisted) {
    // Redirect cout to capture output
    RedirectCoutToBuffer();
    
    // Create a CheckCommand
    CheckCommand checkCmd(*bloomFilter, *blacklist);
    
    // Execute the command with a URL that's not in the blacklist
    checkCmd.execute("2 www.notblacklisted.com");
    
    // Restore cout
    RestoreCout();
    
    // Verify the output
    EXPECT_EQ(outputBuffer.str(), "false\n");
}

// Test CheckCommand with blacklisted URL
TEST_F(CommandTest, CheckCommandBlacklisted) {
    // Add a URL to the blacklist and bloom filter
    bloomFilter->add("www.blacklisted.com");
    blacklist->add("www.blacklisted.com");
    
    // Redirect cout to capture output
    RedirectCoutToBuffer();
    
    // Create a CheckCommand
    CheckCommand checkCmd(*bloomFilter, *blacklist);
    
    // Execute the command with a blacklisted URL
    checkCmd.execute("2 www.blacklisted.com");
    
    // Restore cout
    RestoreCout();
    
    // Verify the output
    EXPECT_EQ(outputBuffer.str(), "true true\n");
}

// Test CheckCommand with bloom filter false positive
TEST_F(CommandTest, CheckCommandFalsePositive) {
    // Create a small bloom filter to increase chance of false positives
    std::vector<HashFunc*> smallHashFuncs;
    smallHashFuncs.push_back(new MultiHash(1));
    BloomFilter smallFilter(4, smallHashFuncs);
    
    // Add multiple URLs to increase bit set density
    smallFilter.add("www.example1.com");
    smallFilter.add("www.example2.com");
    smallFilter.add("www.example3.com");
    
    // Find a URL that gives a false positive
    std::string falsePositiveUrl = "";
    for (int i = 10; i < 100; ++i) {
        std::string testUrl = "www.test" + std::to_string(i) + ".com";
        if (smallFilter.possiblyContain(testUrl)) {
            falsePositiveUrl = testUrl;
            break;
        }
    }
    
    // If we found a false positive URL
    if (!falsePositiveUrl.empty()) {
        // Redirect cout to capture output
        RedirectCoutToBuffer();
        
        // Create a CheckCommand with our small filter
        CheckCommand checkCmd(smallFilter, *blacklist);
        
        // Execute the command with our false positive URL
        checkCmd.execute("2 " + falsePositiveUrl);
        
        // Restore cout
        RestoreCout();
        
        // Verify the output (should be "true false\n" for a false positive)
        EXPECT_EQ(outputBuffer.str(), "true false\n");
    }
    
    // Clean up
    for (auto* hashFunc : smallHashFuncs) {
        delete hashFunc;
    }
}