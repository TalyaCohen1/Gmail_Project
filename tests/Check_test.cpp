#include <gtest/gtest.h>
#include "../src/BloomFilter.h"
#include "../src/URLBlacklist.h"
#include "../src/HashFunc.h" // Include your concrete hash function implementation

// ----------------------------
// Test suite: BloomFilterCheckTest
// ----------------------------

// Test for Bloom filter false positives and URLBlacklist usage
TEST(BloomFilterCheckTest, FalsePositiveAndBlacklistCheck) {
    std::vector<HashFunc*> hashFuncs = {
        new HashFunc(),
        new HashFunc(),
        new HashFunc()
    };
    BloomFilter bf(1000, hashFuncs);
    URLBlacklist ub;

    ub.add("example.com");
    bf.add("example");

    EXPECT_TRUE(bf.possiblyContain("example"));
    EXPECT_FALSE(bf.possiblyContain("not_in_filter"));

    EXPECT_TRUE(ub.contains("example.com"));
    EXPECT_FALSE(ub.contains("not_in_filter"));

    for (auto* func : hashFuncs) delete func;
}

// Test edge cases like empty strings
TEST(BloomFilterCheckTest, EdgeCases) {
    std::vector<HashFunc*> hashFuncs = {
        new HashFunc(),
        new HashFunc(),
        new HashFunc()
    };
    BloomFilter bf(1000, hashFuncs);
    URLBlacklist ub;

    EXPECT_FALSE(bf.possiblyContain(""));
    EXPECT_FALSE(ub.contains(""));

    bf.add("");
    ub.add("");

    EXPECT_TRUE(bf.possiblyContain(""));
    EXPECT_TRUE(ub.contains(""));

    for (auto* func : hashFuncs) delete func;
}

// Test Bloom filter without any insertions
TEST(BloomFilterCheckTest, EmptyFilter) {
    std::vector<HashFunc*> hashFuncs = {
        new HashFunc(),
        new HashFunc(),
        new HashFunc()
    };
    BloomFilter bf(1000, hashFuncs);

    EXPECT_FALSE(bf.possiblyContain("something"));

    for (auto* func : hashFuncs) delete func;
}

// --------------------
// Google Test main()
int main(int argc, char **argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
