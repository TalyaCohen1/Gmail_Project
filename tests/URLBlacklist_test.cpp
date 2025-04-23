#include <gtest/gtest.h>
#include "../src/URLBlacklist.h"
#include <string>
#include <vector>
#include <filesystem>
#include <fstream>

class URLBlacklistTest : public ::testing::Test {
protected:
    void SetUp() override {
        // Create a test directory if it doesn't exist
        if (!std::filesystem::exists("test_data")) {
            std::filesystem::create_directory("test_data");
        }
    }

    void TearDown() override {
        // Clean up test files
        if (std::filesystem::exists("test_data/blacklist_test.txt")) {
            std::filesystem::remove("test_data/blacklist_test.txt");
        }
    }
};

// Test adding a URL to the blacklist
TEST_F(URLBlacklistTest, AddURL) {
    URLBlacklist blacklist;
    
    // Add a URL
    blacklist.add("www.example.com");
    
    // Check if the URL is contained
    EXPECT_TRUE(blacklist.contains("www.example.com"));
}

// Test adding duplicate URLs
TEST_F(URLBlacklistTest, AddDuplicateURL) {
    URLBlacklist blacklist;
    
    // Add the same URL twice
    blacklist.add("www.example.com");
    blacklist.add("www.example.com");
    
    // Check if the URL is contained and the list size is 1
    EXPECT_TRUE(blacklist.contains("www.example.com"));
    EXPECT_EQ(blacklist.getBlacklist().size(), 1);
}

// Test checking for non-existent URL
TEST_F(URLBlacklistTest, CheckNonExistentURL) {
    URLBlacklist blacklist;
    
    // Add a URL
    blacklist.add("www.example.com");
    
    // Check if a different URL is not contained
    EXPECT_FALSE(blacklist.contains("www.different.com"));
}

// Test saving and loading the blacklist
TEST_F(URLBlacklistTest, SaveAndLoad) {
    URLBlacklist blacklist;
    
    // Add some URLs
    blacklist.add("www.example1.com");
    blacklist.add("www.example2.com");
    
    // Save the blacklist to a file
    blacklist.saveToFile("test_data/blacklist_test.txt");
    
    // Create a new blacklist
    URLBlacklist newBlacklist;
    
    // Load the blacklist from the file
    newBlacklist.loadFromFile("test_data/blacklist_test.txt");
    
    // Check if the new blacklist contains the same URLs
    EXPECT_TRUE(newBlacklist.contains("www.example1.com"));
    EXPECT_TRUE(newBlacklist.contains("www.example2.com"));
    EXPECT_EQ(newBlacklist.getBlacklist().size(), 2);
}

// Test getBlacklist method
TEST_F(URLBlacklistTest, GetBlacklist) {
    URLBlacklist blacklist;
    
    // Add some URLs
    blacklist.add("www.example1.com");
    blacklist.add("www.example2.com");
    
    // Get the blacklist and check its content
    const std::vector<std::string>& list = blacklist.getBlacklist();
    EXPECT_EQ(list.size(), 2);
    EXPECT_EQ(list[0], "www.example1.com");
    EXPECT_EQ(list[1], "www.example2.com");
}

// Test loading from a non-existent file
TEST_F(URLBlacklistTest, LoadFromNonExistentFile) {
    URLBlacklist blacklist;
    
    // Try to load from a non-existent file
    blacklist.loadFromFile("test_data/non_existent_file.txt");
    
    // Check that the blacklist is empty
    EXPECT_EQ(blacklist.getBlacklist().size(), 0);
}

// Test saving to an invalid path
TEST_F(URLBlacklistTest, SaveToInvalidPath) {
    URLBlacklist blacklist;
    
    // Add a URL
    blacklist.add("www.example.com");
    
    // Try to save to an invalid path (should handle the error gracefully)
    blacklist.saveToFile("/invalid/path/file.txt");
    
    // No assertion needed as we're just testing that it doesn't crash
}