#include <gtest/gtest.h>
#include "../src/ConfigParser.h"
#include "../src/ConfigParser.cpp"
#include <string>
#include <vector>

TEST(ConfigParserTest, ParsesValidLine) {
    ConfigParser parser;
    std::string line = "256 1 2 3";
    ConfigData data = parser.parseLine(line);

    EXPECT_EQ(data.size, 256);
    EXPECT_EQ(data.hashFunc.size(), 3);
    EXPECT_EQ(data.hashFunc[0], 1);
    EXPECT_EQ(data.hashFunc[1], 2);
    EXPECT_EQ(data.hashFunc[2], 3);
    EXPECT_TRUE(data.valid);
}
TEST(ConfigParserTest, ParsesLineWithInvalidSize) {
    ConfigParser parser;
    std::string line = "-1 12 4";
    ConfigData data = parser.parseLine(line);

    EXPECT_EQ(data.size, -1);
    EXPECT_EQ(data.hashFunc.size(), 2);
    EXPECT_EQ(data.hashFunc[0], 12);
    EXPECT_EQ(data.hashFunc[1], 4);
    EXPECT_FALSE(data.valid);
}
TEST(ConfigParserTest, ParsesLineWithNoHashFunctions) {
    ConfigParser parser;
    std::string line = "100";
    ConfigData data = parser.parseLine(line);

    EXPECT_EQ(data.size, 100);
    EXPECT_EQ(data.hashFunc.size(), 0);
    EXPECT_FALSE(data.valid);
}
TEST(ConfigParserTest, ParsesLineWithInvalidHashFunction) {
    ConfigParser parser;
    std::string line = "128 1 -2 3";
    ConfigData data = parser.parseLine(line);

    EXPECT_EQ(data.size, 128);
    EXPECT_EQ(data.hashFunc.size(), 3);
    EXPECT_FALSE(data.valid);
}
TEST(ConfigParserTest, ParsesLineWithNonInteger) {
    ConfigParser parser;
    std::string line = "256 1 a 3";
    ConfigData data = parser.parseLine(line);

    EXPECT_EQ(data.size, 256);
    EXPECT_EQ(data.hashFunc.size(), 2);
    EXPECT_EQ(data.hashFunc[0], 1);
    EXPECT_EQ(data.hashFunc[1], 3);
    EXPECT_FALSE(data.valid);
}
TEST(ConfigParserTest, ParsesLineWithEmptyString) {
    ConfigParser parser;
    std::string line = "";
    ConfigData data = parser.parseLine(line);

    EXPECT_EQ(data.size, 0);
    EXPECT_FALSE(data.valid);
}
