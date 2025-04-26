#include <gtest/gtest.h>
#include "../src/URLBlacklist.h"
#include <string>
#include <vector>
#include <filesystem>
#include <fstream>

// ----------------------------
// Test suite: URLBlacklistTest
// ----------------------------

class URLBlacklistTest : public ::testing::Test {
protected:
    void SetUp() override {
        // Create a test directory if it doesn't exist
        if (!std::filesystem::exists("test_data")) {
            std::filesystem::create_directory("test_data");
        }
    }

    void TearDown() override {
        // Clean up test files after each test
        if (std::filesystem::exists("test_data/blacklist_test.txt")) {
            std::filesystem::remove("test_data/blacklist_test.txt");
        }
    }
};

// ----------------------------
// Tests for adding URLs
// ----------------------------

TEST_F(URLBlacklistTest, AddURL) {
    URLBlacklist blacklist;

    blacklist.add("www.example.com");

    EXPECT_TRUE(blacklist.contains("www.example.com"));
}

TEST_F(URLBlacklistTest, AddDuplicateURL) {
    URLBlacklist blacklist;

    blacklist.add("www.example.com");
    blacklist.add("www.example.com");

    EXPECT_TRUE(blacklist.contains("www.example.com"));
    EXPECT_EQ(blacklist.getBlacklist().size(), 1);
}

// ----------------------------
// Tests for checking URL existence
// ----------------------------

TEST_F(URLBlacklistTest, CheckNonExistentURL) {
    URLBlacklist blacklist;

    blacklist.add("www.example.com");

    EXPECT_FALSE(blacklist.contains("www.different.com"));
}

// ----------------------------
// Tests for saving and loading the blacklist
// ----------------------------

TEST_F(URLBlacklistTest, SaveAndLoad) {
    URLBlacklist blacklist;

    blacklist.add("www.example1.com");
    blacklist.add("www.example2.com");

    blacklist.saveToFile("test_data/blacklist_test.txt");

    URLBlacklist newBlacklist;
    newBlacklist.loadFromFile("test_data/blacklist_test.txt");

    EXPECT_TRUE(newBlacklist.contains("www.example1.com"));
    EXPECT_TRUE(newBlacklist.contains("www.example2.com"));
    EXPECT_EQ(newBlacklist.getBlacklist().size(), 2);
}

// ----------------------------
// Tests for retrieving the blacklist
// ----------------------------

TEST_F(URLBlacklistTest, GetBlacklist) {
    URLBlacklist blacklist;

    blacklist.add("www.example1.com");
    blacklist.add("www.example2.com");

    const std::vector<std::string>& list = blacklist.getBlacklist();
    EXPECT_EQ(list.size(), 2);
    EXPECT_EQ(list[0], "www.example1.com");
    EXPECT_EQ(list[1], "www.example2.com");
}

// ----------------------------
// Tests for loading from a non-existent file
// ----------------------------

TEST_F(URLBlacklistTest, LoadFromNonExistentFile) {
    URLBlacklist blacklist;

    blacklist.loadFromFile("test_data/non_existent_file.txt");

    EXPECT_EQ(blacklist.getBlacklist().size(), 0);
}

// ----------------------------
// Tests for saving to an invalid path
// ----------------------------

TEST_F(URLBlacklistTest, SaveToInvalidPath) {
    URLBlacklist blacklist;

    blacklist.add("www.example.com");

    // Try to save to an invalid path; should handle error gracefully
    blacklist.saveToFile("/invalid/path/file.txt");

    // No assertions needed; test that it does not crash
}
