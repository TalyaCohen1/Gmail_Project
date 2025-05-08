#include <gtest/gtest.h>
#include "../src/MainLoop.h"
#include <fstream>
#include <filesystem>
#include <string>

namespace fs = std::filesystem;

TEST(MainLoopTest, ConstructorCreatesDataDirectoryAndBlacklistFile) {
    std::string config = "256 1 2 3";
    MainLoop mainLoop(config);

    EXPECT_TRUE(fs::exists("data")) << "Directory 'data' was not created.";
    EXPECT_TRUE(fs::exists("data/urlblacklist.txt")) << "'urlblacklist.txt' was not created.";

    std::ifstream file("data/urlblacklist.txt");
    std::string content;
    std::getline(file, content);
    EXPECT_TRUE(content.empty()) << "Blacklist file is not empty.";
}

TEST(MainLoopTest, ConstructorInitializesBloomFilterCorrectly) {
    std::string config = "256 1 2 3";
    MainLoop mainLoop(config);

    EXPECT_EQ(mainLoop.getBloomFilter().getSize(), 256);
    EXPECT_EQ(mainLoop.getBloomFilter().getHashNum(), 3);
}

TEST(MainLoopTest, RunHandlesPostGetDeleteBadCommands) {
    std::string config = "256 1 2 3";
    MainLoop loop(config);

    std::string postResp = loop.run("POST https://example.com");
    EXPECT_EQ(postResp, "201 Created\n");

    std::string getResp1 = loop.run("GET https://example.com");
    EXPECT_EQ(getResp1, "200 Ok\n\ntrue true");

    std::string deleteResp = loop.run("DELETE https://example.com");
    EXPECT_EQ(deleteResp, "200 Ok\n");

    std::string getResp2 = loop.run("GET https://example.com");
    EXPECT_EQ(getResp2, "200 Ok\n\ntrue false");

    std::string badUrl = loop.run("POST invalid_url");
    EXPECT_EQ(badUrl, "400 Bad Request\n");

    std::string badCmd = loop.run("WRONGCMD https://example.com");
    EXPECT_EQ(badCmd, "400 Bad Request\n");

    std::ifstream file("data/urlblacklist.txt");
    std::string line;
    bool foundValid = false;
    while (std::getline(file, line)) {
        if (line == "https://example.com") {
            foundValid = true;
        }
        ASSERT_NE(line, "invalid_url") << "Invalid URL should not be stored.";
    }
    EXPECT_TRUE(foundValid) << "Expected URL not found in blacklist.";
}
