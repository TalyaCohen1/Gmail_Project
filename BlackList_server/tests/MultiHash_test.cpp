#include <gtest/gtest.h>
#include "../include/MultiHash.h"
#include "../include/HashFunc.h"
#include <string>
#include <functional>

class MultiHashTest : public ::testing::Test {
};

// Test hashing with times=1 (standard std::hash)
TEST_F(MultiHashTest, HashOnce) {
    MultiHash hasher(1);
    std::string input = "test";
    
    int result = hasher.execute(input);
    
    // Compare with direct std::hash
    std::hash<std::string> stdHasher;
    int expected = static_cast<int>(stdHasher(input));
    
    EXPECT_EQ(result, expected);
}

// Test consistency of hashing
TEST_F(MultiHashTest, HashConsistency) {
    MultiHash hasher(2);
    std::string input = "consistency";
    
    int result1 = hasher.execute(input);
    int result2 = hasher.execute(input);
    
    EXPECT_EQ(result1, result2);
}

// Test different inputs produce different hashes
TEST_F(MultiHashTest, DifferentInputs) {
    MultiHash hasher(1);
    std::string input1 = "input1";
    std::string input2 = "input2";
    
    int result1 = hasher.execute(input1);
    int result2 = hasher.execute(input2);
    
    EXPECT_NE(result1, result2);
}

// Test with times=0 (should still hash once)
TEST_F(MultiHashTest, ZeroTimes) {
    // This test depends on how you want to handle times=0
    // If it should default to 1, test that
    MultiHash hasher(0);
    std::string input = "test";
    
    int result = hasher.execute(input);
    
    // Compare with direct std::hash
    std::hash<std::string> stdHasher;
    int expected = static_cast<int>(stdHasher(input));
    
    // This might fail if your implementation handles times=0 differently
    EXPECT_EQ(result, expected);
}