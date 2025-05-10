#include <gtest/gtest.h>
#include "../src/DeleteCommand.h"
#include "../src/URLBlacklist.h"
#include <vector>
#include <string>
#include <sstream>

class DeleteCommandTest : public ::testing::Test {
protected:
    void SetUp() override {
        // Create a test URL blacklist
        blacklist = new URLBlacklist();
        
        // Add some test URLs
        blacklist->add("www.example.com");
        blacklist->add("www.test.com");
        blacklist->add("www.sample.com");
    }

    void TearDown() override {
        // Clean up resources
        delete blacklist;
    }
    
    // Redirect cout for testing output
    void RedirectCoutToBuffer() {
        oldCoutBuffer = std::cout.rdbuf();
        std::cout.rdbuf(outputBuffer.rdbuf());
    }
    
    // Restore cout
    void RestoreCout() {
        std::cout.rdbuf(oldCoutBuffer);
    }

    URLBlacklist* blacklist;
    std::stringstream outputBuffer;
    std::streambuf* oldCoutBuffer;
};

// Test deleting an existing URL
TEST_F(DeleteCommandTest, DeleteExistingURL) {
    // Create a DeleteCommand
    DeleteCommand deleteCmd(*blacklist);
    
    // Execute the command with a URL that exists in the blacklist
    std::string result = deleteCmd.execute("www.example.com");
    
    // Verify the URL was removed from the blacklist
    EXPECT_FALSE(blacklist->contains("www.example.com"));
    
    // Verify output indicates successful deletion
    EXPECT_EQ(result, "204 No Content\n");
}

// Test deleting a non-existent URL
TEST_F(DeleteCommandTest, DeleteNonExistentURL) {
    // Create a DeleteCommand
    DeleteCommand deleteCmd(*blacklist);
    
    // Execute the command with a URL that doesn't exist in the blacklist
    std::string result = deleteCmd.execute("3 www.nonexistent.com");
    
    // Verify the blacklist still contains the original URLs
    EXPECT_TRUE(blacklist->contains("www.example.com"));
    EXPECT_TRUE(blacklist->contains("www.test.com"));
    EXPECT_TRUE(blacklist->contains("www.sample.com"));
    
    // Verify output contains not found message
    EXPECT_EQ(result, "404 Not Found\n");
}

// Test deleting all URLs
TEST_F(DeleteCommandTest, DeleteAllURLs) {
    DeleteCommand deleteCmd(*blacklist);
    
    // Delete all URLs one by one
    deleteCmd.execute("www.example.com");
    deleteCmd.execute("www.test.com");
    deleteCmd.execute("www.sample.com");
    
    // Verify the blacklist is empty
    EXPECT_EQ(blacklist->getBlacklist().size(), 0);
}

// Test deleting and then trying to check the URL
TEST_F(DeleteCommandTest, DeleteAndCheckURL) {
    // Delete a URL
    DeleteCommand deleteCmd(*blacklist);
    deleteCmd.execute("www.example.com");
    
    // Verify it was deleted
    EXPECT_FALSE(blacklist->contains("www.example.com"));
    
    // Add it back
    blacklist->add("www.example.com");
    
    // Verify it was added back
    EXPECT_TRUE(blacklist->contains("www.example.com"));
}