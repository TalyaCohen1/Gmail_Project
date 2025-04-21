#include "gtest/gtest.h"
#include "../src/initialize.h"
#include "../src/BloomFilter.h"
#include "../src/hashfunc.h"

TEST(InitializeTest, CreatesBloomFilterWithCorrectSize) {
    int expectedSize = 256;
    std::vector<int> hashIds = {1, 2};

    BloomFilter filter = Initialize::create(expectedSize, hashIds);

    EXPECT_EQ(filter.getSize(), expectedSize);
}
// TEST(InitializeTest, InitializesWithCorrectHashFunctionConfig) {
//     int size = 256;
//     std::vector<int> expectedHashIDs = {1, 2};

//     BloomFilter filter = Initialize::create(size, expectedHashIDs);

//     // Check the number of Hushfuncs.
//     const std::vector<HashFunc*>& hashFuncs = filter.getHashFunctions();
//     ASSERT_EQ(hashFuncs.size(), 1);  // only one HashFunc object for {1,2}

//     // Check that the internal hashIDs match what we expect
//     const std::vector<int>& actualHashIDs = hashFuncs[0]->getHashIDs();
//     EXPECT_EQ(actualHashIDs, expectedHashIDs);
// }
