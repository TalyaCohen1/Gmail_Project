#include <gtest/gtest.h>
#include "../src/BloomFilter.h"
//#include "bloom_filter.h" // Replace with the actual header file for your Bloom Filter implementation

// Test case for adding elements to the Bloom Filter
TEST(BloomFilterTest, AddElement) {
    BloomFilter bf(1000, 3); // Example: 1000 bits, 3 hash functions
    bf.add("test");
    EXPECT_TRUE(bf.contains("test"));
    EXPECT_FALSE(bf.contains("not_in_filter"));
}

// Test case for false positives
TEST(BloomFilterTest, FalsePositive) {
    BloomFilter bf(1000, 3);
    bf.add("example");
    // Since Bloom Filters can have false positives, we can't guarantee "false" for unrelated elements.
    // But we can ensure that added elements are always detected.
    EXPECT_TRUE(bf.contains("example"));
}

// Test case for edge cases
TEST(BloomFilterTest, EdgeCases) {
    BloomFilter bf(1000, 3);
    EXPECT_FALSE(bf.contains("")); // Empty string
    bf.add("");
    EXPECT_TRUE(bf.contains("")); // Now it should contain the empty string
}

int main(int argc, char **argv) {
    ::testing::InitGoogleTest(&argc, argv);
    return RUN_ALL_TESTS();
}