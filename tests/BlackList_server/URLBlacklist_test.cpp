#include <gtest/gtest.h>
#include "../../BlackList_server/include/URLBlacklist.h"
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

// ----------------------------
// Tests for deleting URLs
// ----------------------------

TEST_F(URLBlacklistTest, DeleteURL) {
    URLBlacklist blacklist;

    // Add URLs
    blacklist.add("www.to-delete.com");
    blacklist.add("www.to-keep.com");

    // Save to file
    blacklist.saveToFile("test_data/blacklist_test.txt");

    // Delete a URL
    blacklist.deleteURL("www.to-delete.com", "test_data/blacklist_test.txt");

    // Check that it's no longer in memory
    EXPECT_FALSE(blacklist.contains("www.to-delete.com"));
    EXPECT_TRUE(blacklist.contains("www.to-keep.com"));

    // Reload from file to ensure persistence
    URLBlacklist loaded;
    loaded.loadFromFile("test_data/blacklist_test.txt");

    EXPECT_FALSE(loaded.contains("www.to-delete.com"));
    EXPECT_TRUE(loaded.contains("www.to-keep.com"));
    EXPECT_EQ(loaded.getBlacklist().size(), 1);
}

// -----------------------------
// Tests for deleting a URL that doesn't exist
// -----------------------------

TEST_F(URLBlacklistTest, DeleteNonExistentURL) {
    URLBlacklist blacklist;

    blacklist.add("www.keep-me.com");
    blacklist.saveToFile("test_data/blacklist_test.txt");

    // Attempt to delete a URL that doesn't exist
    blacklist.deleteURL("www.not-in-list.com", "test_data/blacklist_test.txt");

    // Should not affect the original URL
    EXPECT_TRUE(blacklist.contains("www.keep-me.com"));
    EXPECT_FALSE(blacklist.contains("www.not-in-list.com"));

    // Reload to confirm file wasn't incorrectly changed
    URLBlacklist loaded;
    loaded.loadFromFile("test_data/blacklist_test.txt");

    EXPECT_TRUE(loaded.contains("www.keep-me.com"));
    EXPECT_FALSE(loaded.contains("www.not-in-list.com"));
    EXPECT_EQ(loaded.getBlacklist().size(), 1);
}

// -----------------------------
// Tests for deleting from an empty blacklist
// -----------------------------

TEST_F(URLBlacklistTest, DeleteFromEmptyBlacklist) {
    URLBlacklist blacklist;

    // Attempt to delete from empty blacklist
    blacklist.deleteURL("www.anything.com", "test_data/blacklist_test.txt");

    // Blacklist should still be empty
    EXPECT_EQ(blacklist.getBlacklist().size(), 0);

    // File should be empty too
    std::ifstream infile("test_data/blacklist_test.txt");
    std::string line;
    EXPECT_FALSE(std::getline(infile, line));  // Should be empty
}

// -----------------------------
// Tests for deleting a URL from a large blacklist
// -----------------------------

TEST_F(URLBlacklistTest, DeleteURLFromLargeBlacklist) {
    URLBlacklist blacklist;

    // Add a large number of URLs
    for (int i = 0; i < 1000; ++i) {
        blacklist.add("www.example" + std::to_string(i) + ".com");
    }

    // Delete a specific URL
    std::string urlToDelete = "www.example500.com";
    blacklist.deleteURL(urlToDelete, "test_data/blacklist_test.txt");

    // The deleted URL should no longer exist
    EXPECT_FALSE(blacklist.contains(urlToDelete));

    // The size should be reduced by one
    EXPECT_EQ(blacklist.getBlacklist().size(), 999);
}
