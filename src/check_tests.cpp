#include <gtest/gtest.h>
#include "BloomFilter.h"
#include "StdHash.h"

TEST(BloomFilterTest, AddSingleUrl) {
    std::shared_ptr<HashFunc> hash = std::make_shared<StdHash>();
    std::vector<std::shared_ptr<HashFunc>> hashes = { hash };
    BloomFilter bf(100, hashes);

    bf.add("www.example.com");

    EXPECT_TRUE(bf.mightContain("www.example.com"));
}
