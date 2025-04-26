#include <gtest/gtest.h>
#include "../src/BloomFilter.h"
#include "../src/AddCommand.h"
#include "../src/CheckCommand.h"
#include "../src/URLBlacklist.h"
#include "../src/HashFunc.h"
#include "../src/MultiHash.h"
#include "../src/App.h"
#include "../src/ConfigParser.h"
#include "../src/ICommand.h"
#include "../src/MainLoop.h"
#include <vector>
#include <string>
#include <fstream>

// ----------------------------
// Test suite: BloomFilterCheckTest
// ----------------------------

// ----------------------------
// Test case: FalsePositiveAndBlacklistCheck
// ----------------------------

TEST(BloomFilterCheckTest, FalsePositiveAndBlacklistCheck) {
    std::vector<HashFunc*> hashFuncs = {
        new MultiHash(1),
        new MultiHash(1),
        new MultiHash(1)
    };
    BloomFilter bf(1000, hashFuncs);
    URLBlacklist ub;

    ub.add("example.com");
    bf.add("example");

    // Test Bloom filter check
    EXPECT_TRUE(bf.possiblyContain("example"));
    EXPECT_FALSE(bf.possiblyContain("not_in_filter"));

    // Test URLBlacklist check
    EXPECT_TRUE(ub.contains("example.com"));
    EXPECT_FALSE(ub.contains("not_in_filter"));

    // Cleanup hash functions
    for (auto* func : hashFuncs) delete func;
}

// ----------------------------
// Test case: EdgeCases
// ----------------------------

TEST(BloomFilterCheckTest, EdgeCases) {
    std::vector<HashFunc*> hashFuncs = {
        new MultiHash(1),
        new MultiHash(1),
        new MultiHash(1)
    };
    BloomFilter bf(1000, hashFuncs);
    URLBlacklist ub;

    // Test Bloom filter and URLBlacklist with empty strings
    EXPECT_FALSE(bf.possiblyContain(""));
    EXPECT_FALSE(ub.contains(""));

    // Add empty string to Bloom filter and URLBlacklist
    bf.add("");
    ub.add("");

    // Test Bloom filter and URLBlacklist after adding empty strings
    EXPECT_TRUE(bf.possiblyContain(""));
    EXPECT_TRUE(ub.contains(""));

    // Cleanup hash functions
    for (auto* func : hashFuncs) delete func;
}

// ----------------------------
// Test case: EmptyFilter
// ----------------------------

TEST(BloomFilterCheckTest, EmptyFilter) {
    std::vector<HashFunc*> hashFuncs = {
        new MultiHash(1),
        new MultiHash(1),
        new MultiHash(1)
    };
    BloomFilter bf(1000, hashFuncs);

    // Test Bloom filter with no insertions
    EXPECT_FALSE(bf.possiblyContain("something"));

    // Cleanup hash functions
    for (auto* func : hashFuncs) delete func;
}
