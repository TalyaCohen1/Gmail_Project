#include <gtest/gtest.h>
#include "../src/ConfigParser.h"
#include "../src/ConfigParser.cpp"
#include <string>
#include <vector>

TEST(ConfigParserTest, ParsesValidLine) {
    std::string line = "256 1 2 3";
    ConfigParser parser = parser.parseLine(line);

    EXPECT_EQ(parser.getSize(), 256);
    EXPECT_EQ(parser.getHushFunc().size(), 3);
    EXPECT_EQ(parser.getHushFunc(),std::vector<int>({1, 2, 3}));
    EXPECT_TRUE(parser.isValid());
}
TEST(ConfigParserTest, ParsesLineWithInvalidSize) {
    std::string line = "-1 12 4";
    ConfigParser parser = parser.parseLine(line);

    EXPECT_EQ(parser.getSize(), -1);
    EXPECT_EQ(parser.getHushFunc().size(), 2);
    EXPECT_EQ(parser.getHushFunc(),std::vector<int>({12,4}));
    EXPECT_FALSE(parser.isValid());
}
TEST(ConfigParserTest, ParsesLineWithNoHashFunctions) {
    std::string line = "100";
    ConfigParser parser = parser.parseLine(line);

    EXPECT_EQ(parser.getSize(), 100);
    EXPECT_EQ(parser.getHushFunc().size(), 0);
    EXPECT_FALSE(parser.isValid());
}
TEST(ConfigParserTest, ParsesLineWithInvalidHashFunction) {
    std::string line = "128 1 -2 3";
    ConfigParser parser = parser.parseLine(line);

    EXPECT_EQ(parser.getSize(), 128);
    EXPECT_EQ(parser.getHushFunc().size(), 3);
    EXPECT_FALSE(parser.isValid());
}
TEST(ConfigParserTest, ParsesLineWithNonInteger) {
    std::string line = "256 1 a 3";
    ConfigParser parser = parser.parseLine(line);

    EXPECT_EQ(parser.getSize(), 256);
    EXPECT_EQ(parser.getHushFunc().size(), 2);
    EXPECT_EQ(parser.getHushFunc(),std::vector<int>({1, 3}));
    EXPECT_FALSE(parser.isValid());
}
TEST(ConfigParserTest, ParsesLineWithEmptyString) {
    std::string line = "";
    ConfigParser parser = parser.parseLine(line);

    EXPECT_EQ(parser.getSize(), 0);
    EXPECT_FALSE(parser.isValid());
}
