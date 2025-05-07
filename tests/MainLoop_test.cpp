#include <gtest/gtest.h>
#include "../src/MainLoop.h"
#include "../src/BloomFilter.h"
#include "../src/MultiHash.h"
#include "../src/PostCommand.h"
#include "../src/GetCommand.h"
#include "../src/ConfigParser.h"
#include "../src/URLBlacklist.h"
#include "../src/BadRequest.h>
#include <iostream>
#include <sstream>
#include <map>
#include <fstream>
#include <filesystem>
#include <regex>
#include <string>
#include <vector>
#include <utility>

namespace fs = std::filesystem;

// Helper function to prepare input for MainLoop tests
void prepareMainLoopInput(const std::string& config = "256 1 2 3\n") {
    if (fs::exists("data")) {
        fs::remove_all("data");
    }
    std::istringstream* input = new std::istringstream(config);
    std::cin.rdbuf(input->rdbuf());
}

// ----------------------------
// Test suite: MainLoopTest
// ----------------------------

// Test the constructor and initialization of the MainLoop class

TEST(MainLoopTest, ConstructorCreatesDataDirectoryAndBlacklistFile) {
    prepareMainLoopInput();
    MainLoop mainLoop;

    // Verify that 'data' directory is created
    EXPECT_TRUE(fs::exists("data")) << "The 'data' directory was not created.";
    // Verify that 'urlblacklist.txt' file is created
    EXPECT_TRUE(fs::exists("data/urlblacklist.txt")) << "The 'urlblacklist.txt' file was not created.";

    // Verify that 'urlblacklist.txt' is empty
    std::ifstream blacklistFile("data/urlblacklist.txt");
    EXPECT_TRUE(blacklistFile.is_open()) << "Failed to open 'urlblacklist.txt'.";
    std::string content;
    std::getline(blacklistFile, content);
    EXPECT_TRUE(content.empty()) << "'urlblacklist.txt' is not empty.";
    blacklistFile.close();
}

TEST(MainLoopTest, ConstructorInitializesBloomFilter) {
    prepareMainLoopInput();
    MainLoop mainLoop;

    // Verify the size and number of hash functions for the Bloom filter
    EXPECT_EQ(mainLoop.getBloomFilter().getSize(), 256);
    EXPECT_EQ(mainLoop.getBloomFilter().getHashNum(), 3);
}
TEST(MainLoopTest, FullRunWithAllCommands) {
    // ניקוי סביבת העבודה
    if (fs::exists("data")) {
        fs::remove_all("data");
    }

    // הכנה של שורת קונפיגורציה תקינה
    std::string configLine = "256 1 2 3";
    MainLoop loop(configLine);

    // שליחת פקודות
    std::string result1 = loop.run("POST https://example.com");
    EXPECT_EQ(result1, "201 Created\n");

    std::string result2 = loop.run("GET https://example.com");
    EXPECT_EQ(result2, "200 Ok\n\ntrue true");

    std::string result3 = loop.run("DELETE https://example.com");
    EXPECT_EQ(result3, "200 Ok\n");

    std::string result4 = loop.run("GET https://example.com");
    EXPECT_EQ(result4, "200 Ok\n\ntrue false");

    std::string result5 = loop.run("POST invalid_url");
    EXPECT_EQ(result5, "400 Bad Request\n");

    std::string result6 = loop.run("WRONGCMD https://example.com");
    EXPECT_EQ(result6, "400 Bad Request\n");

    // בדיקה שה־blacklist לא שומר את ה־invalid
    std::ifstream file("data/urlblacklist.txt");
    std::string line;
    bool found = false;
    while (std::getline(file, line)) {
        if (line == "https://example.com") {
            found = true;
        }
        ASSERT_NE(line, "invalid_url") << "Invalid URL should not be stored";
    }
    EXPECT_TRUE(found) << "Expected URL not found in blacklist";
}
