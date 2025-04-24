#include <gtest/gtest.h>
#include "../src/BloomFilter.h"
#include "../src/URLBlacklist.h"
#include "../src/MultiHash.h"
 // Changed from HashFunc to MultiHash

// Ensure all namespaces or classes are properly closed

// ----------------------------
// Test suite: BloomFilterAddTest
// ----------------------------

// Test adding and checking a known element
TEST(BloomFilterAddTest, AddElement)
{
    std::vector<HashFunc*> hashFuncs = {
        new MultiHash(1), // Using MultiHash instead of HashFunc
        new MultiHash(2),
        new MultiHash(3)
    };
    BloomFilter bf(1000, hashFuncs);
    bf.add("test");

    EXPECT_TRUE(bf.possiblyContain("test"));
    // No need to delete hashFuncs as BloomFilter destructor will handle it
}

// Test duplicate insertions
TEST(BloomFilterAddTest, DuplicateInsertions) {
    std::vector<HashFunc*> hashFuncs = {
        new MultiHash(1),
        new MultiHash(2),
        new MultiHash(3)
    };
    BloomFilter bf(1000, hashFuncs);
    bf.add("duplicate");
    bf.add("duplicate");

    EXPECT_TRUE(bf.possiblyContain("duplicate"));

    // No need to delete hashFuncs as BloomFilter destructor will handle it
}

// Test large number of insertions
TEST(BloomFilterAddTest, ManyInsertions) {
    std::vector<HashFunc*> hashFuncs = {
        new MultiHash(1),
        new MultiHash(2),
        new MultiHash(3)
    };
    BloomFilter bf(10000, hashFuncs);
    for (int i = 0; i < 500; ++i) {
        bf.add("item" + std::to_string(i));
    }

    for (int i = 0; i < 500; ++i) {
        EXPECT_TRUE(bf.possiblyContain("item" + std::to_string(i)));
    }

    // No need to delete hashFuncs as BloomFilter destructor will handle it
}

// Test special characters in strings
TEST(BloomFilterAddTest, SpecialCharacters) {
    std::vector<HashFunc*> hashFuncs = {
        new MultiHash(1),
        new MultiHash(2), 
        new MultiHash(3)
    };
    BloomFilter bf(1000, hashFuncs);
    std::string special = "!@#$%^&*()_+|}{:?><";
    bf.add(special);

    EXPECT_TRUE(bf.possiblyContain(special));

    // No need to delete hashFuncs as BloomFilter destructor will handle it
}

