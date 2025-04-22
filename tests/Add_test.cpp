#include <gtest/gtest.h>
#include "../src/BloomFilter.h"
#include "../src/URLBlacklist.h" // Assuming you have this header for URLBlacklist

// ----------------------------
// Test suite: BloomFilterAddTest
// ----------------------------

// Test adding and checking a known element
TEST(BloomFilterAddTest, AddElement) {
    BloomFilter bf(1000, 3); // Example: 1000 bits, 3 hash functions
    bf.add("test");

    EXPECT_TRUE(bf.possiblyContain("test"));
}

// Test duplicate insertions
TEST(BloomFilterAddTest, DuplicateInsertions) {
    BloomFilter bf(1000, 3);
    bf.add("duplicate");
    bf.add("duplicate");

    EXPECT_TRUE(bf.possiblyContain("duplicate"));
}

// Test large number of insertions
TEST(BloomFilterAddTest, ManyInsertions) {
    BloomFilter bf(10000, 5); // Larger filter
    for (int i = 0; i < 500; ++i) {
        bf.add("item" + std::to_string(i));
    }

    for (int i = 0; i < 500; ++i) {
        EXPECT_TRUE(bf.possiblyContain("item" + std::to_string(i)));
    }
}

// Test special characters in strings
TEST(BloomFilterAddTest, SpecialCharacters) {
    BloomFilter bf(1000, 3);
    std::string special = "!@#$%^&*()_+|}{:?><";
    bf.add(special);

    EXPECT_TRUE(bf.possiblyContain(special));
}

// --------------------
// Google Test main()
int main(int argc, char **argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}
