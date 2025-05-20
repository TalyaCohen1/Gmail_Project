#include "../../BlackList_server/include/CommandParser.h"
#include <gtest/gtest.h>

TEST(CommandParserTest, ValidPostCommand) {
    std::string input = "POST www.example.com";
    CommandParser parser(input);

    EXPECT_EQ(parser.getCommand(), "POST");
    EXPECT_EQ(parser.getUrl(), "www.example.com");
    EXPECT_TRUE(parser.isValidCommand());
    EXPECT_TRUE(parser.isValidUrl());
}

TEST(CommandParserTest, InvalidCommand) {
    std::string input = "FOO www.example.com";
    CommandParser parser(input);

    EXPECT_EQ(parser.getCommand(), "FOO");
    EXPECT_FALSE(parser.isValidCommand());
}

TEST(CommandParserTest, InvalidUrl) {
    std::string input = "POST not_a_url";
    CommandParser parser(input);

    EXPECT_EQ(parser.getCommand(), "POST");
    EXPECT_FALSE(parser.isValidUrl());
}

TEST(CommandParserTest, ValidDeleteCommand) {
    std::string input = "DELETE http://test.com";
    CommandParser parser(input);

    EXPECT_EQ(parser.getCommand(), "DELETE");
    EXPECT_EQ(parser.getUrl(), "http://test.com");
    EXPECT_TRUE(parser.isValidCommand());
    EXPECT_TRUE(parser.isValidUrl());
}
