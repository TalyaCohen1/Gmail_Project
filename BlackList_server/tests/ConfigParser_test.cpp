#include <gtest/gtest.h>
#include "../include/ConfigParser.h"
#include <string>
#include <vector>

// ----------------------------
// Test suite: ConfigParserTest
// ----------------------------

// Test parsing a valid line with size and hash functions
TEST(ConfigParserTest, ParsesValidLine) {
    ConfigParser parser;
    std::string line = "256 1 2 3";
    parser.parseLine(line);

    // Verify the parsed size and hash functions
    EXPECT_EQ(parser.getSize(), 256);
    EXPECT_EQ(parser.getHashFunc().size(), 3);
    EXPECT_EQ(parser.getHashFunc(), std::vector<int>({1, 2, 3}));
    EXPECT_TRUE(parser.isValid());
}

// Test parsing a line with an invalid size
TEST(ConfigParserTest, ParsesLineWithInvalidSize) {
    ConfigParser parser;
    std::string line = "-1 12 4";
    parser.parseLine(line);

    // Verify the invalid size and empty hash functions
    EXPECT_EQ(parser.getSize(), 0);
    EXPECT_EQ(parser.getHashFunc(), std::vector<int>({}));
    EXPECT_FALSE(parser.isValid());
}

// Test parsing a line with no hash functions
TEST(ConfigParserTest, ParsesLineWithNoHashFunctions) {
    ConfigParser parser;
    std::string line = "100";
    parser.parseLine(line);

    // Verify the size and ensure no hash functions are parsed
    EXPECT_EQ(parser.getSize(), 100);
    EXPECT_EQ(parser.getHashFunc().size(), 0);
    EXPECT_FALSE(parser.isValid());
}

// Test parsing a line with an invalid hash function
TEST(ConfigParserTest, ParsesLineWithInvalidHashFunction) {
    ConfigParser parser;
    std::string line = "128 1 -2 3";
    parser.parseLine(line);

    // Verify that the invalid hash function results in no valid data
    EXPECT_EQ(parser.getSize(), 0);
    EXPECT_EQ(parser.getHashFunc().size(), 0);
    EXPECT_FALSE(parser.isValid());
}

// Test parsing a line with a non-integer value
TEST(ConfigParserTest, ParsesLineWithNonInteger) {
    ConfigParser parser;
    std::string line = "256 1 a 3";
    parser.parseLine(line);

    // Verify the invalid hash function due to non-integer value
    EXPECT_EQ(parser.getSize(), 0);
    EXPECT_EQ(parser.getHashFunc().size(), 0);
    EXPECT_EQ(parser.getHashFunc(), std::vector<int>({}));
    EXPECT_FALSE(parser.isValid());
}

// Test parsing an empty line
TEST(ConfigParserTest, ParsesLineWithEmptyString) {
    ConfigParser parser;
    std::string line = "";
    parser.parseLine(line);

    // Verify that an empty line results in size 0 and invalid
    EXPECT_EQ(parser.getSize(), 0);
    EXPECT_FALSE(parser.isValid());
}
