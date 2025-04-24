#include <gtest/gtest.h>
#include "../src/MainLoop.h"
#include "../src/BloomFilter.h"
#include "../src/MultiHash.h"
#include "../src/AddCommand.h"
#include "../src/CheckCommand.h"
#include "../src/ConfigParser.h"
#include "../src/URLBlacklist.h"
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

    EXPECT_TRUE(fs::exists("data")) << "The 'data' directory was not created.";
    EXPECT_TRUE(fs::exists("data/urlblacklist.txt")) << "The 'urlblacklist.txt' file was not created.";

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

    EXPECT_EQ(mainLoop.getBloomFilter().getSize(), 256);
    EXPECT_EQ(mainLoop.getBloomFilter().getHashFunctions().size(), 3);
}

TEST(MainLoopTest, isValidCommandRecognizesValidCommands) {
    prepareMainLoopInput();
    MainLoop mainLoop;

    EXPECT_TRUE(mainLoop.isValidCommand(1));
    EXPECT_TRUE(mainLoop.isValidCommand(2));
}

TEST(MainLoopTest, isValidCommandRejectsInvalidCommands) {
    prepareMainLoopInput();
    MainLoop mainLoop;

    EXPECT_FALSE(mainLoop.isValidCommand(0));
    EXPECT_FALSE(mainLoop.isValidCommand(3));
    EXPECT_FALSE(mainLoop.isValidCommand(-1));
}

TEST(MainLoopTest, isValidURLRecognizesValidURLs) {
    prepareMainLoopInput();
    MainLoop mainLoop;

    EXPECT_TRUE(mainLoop.isValidURL("https://example.com"));
    EXPECT_TRUE(mainLoop.isValidURL("http://example.co.uk"));
}

TEST(MainLoopTest, isValidURLRejectsInvalidURLs) {
    prepareMainLoopInput();
    MainLoop mainLoop;

    EXPECT_FALSE(mainLoop.isValidURL("justtext"));
    EXPECT_FALSE(mainLoop.isValidURL("http//invalid"));
}

// Tests for the run() method
TEST(MainLoopTest, Run_MatchesExample1Output) {
    if (fs::exists("data")) {
        fs::remove_all("data");
    }

    std::istringstream input(
        "a\n" // Incorrect configuration
        "8 1 2\n" // Correct configuration
        "2 www.example.com0\n" // Not listed
        "x\n" // Incorrect command
        "1 www.example.com0\n" // Added
        "2 www.example.com0\n" // listed
        "2 www.example.com1\n" // Not listed
        "2 www.example.com11\n" // Not listed
    );

    std::cin.rdbuf(input.rdbuf());

    std::ostringstream output;
    std::streambuf* oldCout = std::cout.rdbuf(output.rdbuf());

    MainLoop loop;
    loop.run();

    std::cout.rdbuf(oldCout);

    std::string expectedOutput =
        "false\n"
        "true true\n"
        "false\n"
        "true false\n";

    // Normalize output (remove prompts and empty lines if needed)
    std::string actual = output.str();
    // remove "Enter configuration: " after first time (already in expected)
    size_t pos;
    while ((pos = actual.find("Enter configuration: ")) != std::string::npos && pos > 0) {
        actual.erase(pos, std::string("Enter configuration: ").length());
    }

    // remove empty lines
    actual.erase(std::remove(actual.begin(), actual.end(), '\r'), actual.end());

    EXPECT_NE(actual.find(expectedOutput), std::string::npos)
        << "Expected output not found.\nExpected:\n" << expectedOutput << "\nGot:\n" << actual;
}

TEST(MainLoopTest, Run_MatchesExample3Output) {
    if (fs::exists("data")) {
        fs::remove_all("data");
    }

    std::istringstream input(
        "8 2\n" // Correct configuration
        "1 www.example.com0\n" // Added
        "2 www.example.com0\n" // listed
        "2 www.example.com4\n" // Not listed
    );

    std::cin.rdbuf(input.rdbuf());

    std::ostringstream output;
    std::streambuf* oldCout = std::cout.rdbuf(output.rdbuf());

    MainLoop loop;
    loop.run();

    std::cout.rdbuf(oldCout);

    std::string expectedOutput =
        "true true\n"
        "true false\n";

    std::string actual = output.str();
    size_t pos;
    while ((pos = actual.find("Enter configuration: ")) != std::string::npos && pos > 0) {
        actual.erase(pos, std::string("Enter configuration: ").length());
    }

    actual.erase(std::remove(actual.begin(), actual.end(), '\r'), actual.end());

    EXPECT_NE(actual.find(expectedOutput), std::string::npos)
        << "Expected output not found.\nExpected:\n" << expectedOutput << "\nGot:\n" << actual;
}
