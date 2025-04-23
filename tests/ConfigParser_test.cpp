#include <gtest/gtest.h>
#include "../src/ConfigParser.h"
#include <string>
#include <vector>

TEST(ConfigParserTest, ParsesValidLine) {
    ConfigParser parser;
    std::string line = "256 1 2 3";
    parser.parseLine(line);

    EXPECT_EQ(parser.getSize(), 256);
    EXPECT_EQ(parser.getHashFunc().size(), 3);
    EXPECT_EQ(parser.getHashFunc(), std::vector<int>({1, 2, 3}));
    EXPECT_TRUE(parser.isValid());
}

TEST(ConfigParserTest, ParsesLineWithInvalidSize) {
    ConfigParser parser;
    std::string line = "-1 12 4";
    parser.parseLine(line);

    EXPECT_EQ(parser.getSize(), -1);
    EXPECT_EQ(parser.getHashFunc().size(), 2);
    EXPECT_EQ(parser.getHashFunc(), std::vector<int>({12, 4}));
    EXPECT_FALSE(parser.isValid());
}

TEST(ConfigParserTest, ParsesLineWithNoHashFunctions) {
    ConfigParser parser;
    std::string line = "100";
    parser.parseLine(line);

    EXPECT_EQ(parser.getSize(), 100);
    EXPECT_EQ(parser.getHashFunc().size(), 0);
    EXPECT_FALSE(parser.isValid());
}

TEST(ConfigParserTest, ParsesLineWithInvalidHashFunction) {
    ConfigParser parser;
    std::string line = "128 1 -2 3";
    parser.parseLine(line);

    EXPECT_EQ(parser.getSize(), 128);
    EXPECT_EQ(parser.getHashFunc().size(), 2);
    EXPECT_FALSE(parser.isValid());
}

TEST(ConfigParserTest, ParsesLineWithNonInteger) {
    ConfigParser parser;
    std::string line = "256 1 a 3";
    parser.parseLine(line);

    EXPECT_EQ(parser.getSize(), 256);
    EXPECT_EQ(parser.getHashFunc().size(), 2); 
    EXPECT_EQ(parser.getHashFunc(), std::vector<int>({1, 3}));
    EXPECT_FALSE(parser.isValid());
}

TEST(ConfigParserTest, ParsesLineWithEmptyString) {
    ConfigParser parser;
    std::string line = "";
    parser.parseLine(line);

    EXPECT_EQ(parser.getSize(), 0); 
    EXPECT_FALSE(parser.isValid());
}
