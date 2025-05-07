#include <gtest/gtest.h>
#include "../src/PostCommand.h"
#include "../src/BloomFilter.h"
#include "../src/URLBlacklist.h"
#include "../src/MultiHash.h"
#include <fstream>

TEST(PostCommandTest, AddsUrlAndReturnsCreated) {
    std::vector<HashFunc*> hashFuncs = {
        new MultiHash(1),
        new MultiHash(2),
        new MultiHash(3)
    };

    BloomFilter bloom(1000, hashFuncs);
    URLBlacklist blacklist;

    PostCommand cmd(bloom, blacklist);
    std::string url = "www.example.com";

    std::string result = cmd.execute(url);

    EXPECT_EQ(result, "201 Created\n");
    EXPECT_TRUE(bloom.possiblyContain(url));
    EXPECT_TRUE(blacklist.contains(url));

    // Check file was written (optional)
    std::ifstream infile("data/urlblacklist.txt");
    std::string line;
    bool found = false;
    while (std::getline(infile, line)) {
        if (line == url) {
            found = true;
            break;
        }
    }
    infile.close();
    EXPECT_TRUE(found);
}
