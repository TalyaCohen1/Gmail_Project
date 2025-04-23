#include <gtest/gtest.h>
#include "../src/BloomFilter.h"
#include "../src/URLBlacklist.h"
#include "../src/HashFunc.h" 

// ----------------------------
// Test suite: BloomFilterAddTest
// ----------------------------

// Test adding and checking a known element
TEST(BloomFilterAddTest, AddElement) {
    std::vector<HashFunc*> hashFuncs = {
        new HashFunc(),
        new HashFunc(),
        new HashFunc()
    };
    BloomFilter bf(1000, hashFuncs);
    bf.add("test");

    EXPECT_TRUE(bf.possiblyContain("test"));

    for (auto* func : hashFuncs) delete func;
}

// Test duplicate insertions
TEST(BloomFilterAddTest, DuplicateInsertions) {
    std::vector<HashFunc*> hashFuncs = {
        new HashFunc(),
        new HashFunc(),
        new HashFunc()
    };
    BloomFilter bf(1000, hashFuncs);
    bf.add("duplicate");
    bf.add("duplicate");

    EXPECT_TRUE(bf.possiblyContain("duplicate"));

    for (auto* func : hashFuncs) delete func;
}

// Test large number of insertions
TEST(BloomFilterAddTest, ManyInsertions) {
    std::vector<HashFunc*> hashFuncs = {
        new HashFunc(),
        new HashFunc(),
        new HashFunc()
    };
    BloomFilter bf(10000, hashFuncs);
    for (int i = 0; i < 500; ++i) {
        bf.add("item" + std::to_string(i));
    }

    for (int i = 0; i < 500; ++i) {
        EXPECT_TRUE(bf.possiblyContain("item" + std::to_string(i)));
    }

    for (auto* func : hashFuncs) delete func;
}

// Test special characters in strings
TEST(BloomFilterAddTest, SpecialCharacters) {
    std::vector<HashFunc*> hashFuncs = {
        HashFunc(),
        HashFunc(),
        HashFunc()
    };
    BloomFilter bf(1000, hashFuncs);
    std::string special = "!@#$%^&*()_+|}{:?><";
    bf.add(special);

    EXPECT_TRUE(bf.possiblyContain(special));

    for (auto* func : hashFuncs) delete func;
}

// --------------------
// Google Test main()
int main(int argc, char **argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}

