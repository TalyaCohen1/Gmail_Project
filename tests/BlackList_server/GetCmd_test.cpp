#include <gtest/gtest.h>
#include "../../BlackList_server/include/BloomFilter.h"
#include "../../BlackList_server/include/GetCommand.h"
#include "../../BlackList_server/include/PostCommand.h"
#include "../../BlackList_server/include/URLBlacklist.h"
#include "../../BlackList_server/include/HashFunc.h"
#include "../../BlackList_server/include/MultiHash.h"
//#include "../../BlackList_server/include/App.h"
#include "../../BlackList_server/include/ConfigParser.h"
#include "../../BlackList_server/include/ICommand.h"
#include "../../BlackList_server/include/MainLoop.h"
#include <vector>
#include <string>
#include <fstream>

// ----------------------------
// Test suite: BloomFilterCheckTest
// ----------------------------


// ----------------------------
// Test case: Found in both BloomFilter and Blacklist
// ----------------------------
TEST(BloomFilterGetTest, FoundInBloomAndBlacklist) {
    std::vector<HashFunc*> hashFuncs = {
        new MultiHash(1),
        new MultiHash(2),
        new MultiHash(3)
    };
    BloomFilter bf(1000, hashFuncs);
    URLBlacklist ub;

    std::string url = "example.com";
    bf.add(url);
    ub.add(url);

    GetCommand cmd(bf, ub);
    std::string result = cmd.execute(url);

    EXPECT_EQ(result, "200 Ok\n\ntrue true");

    for (auto* f : hashFuncs) delete f;
}

// ----------------------------
// Test case: Found in BloomFilter but not in Blacklist (false positive)
// ----------------------------
TEST(BloomFilterGetTest, FoundInBloomButNotInBlacklist) {
    std::vector<HashFunc*> hashFuncs = {
        new MultiHash(1),
        new MultiHash(2),
        new MultiHash(3)
    };
    BloomFilter bf(1000, hashFuncs);
    URLBlacklist ub;

    std::string url = "falsepositive.com";
    bf.add(url);

    GetCommand cmd(bf, ub);
    std::string result = cmd.execute(url);

    EXPECT_EQ(result, "200 Ok\n\ntrue false");

    for (auto* f : hashFuncs) delete f;
}

// ----------------------------
// Test case: Not found at all
// ----------------------------
TEST(BloomFilterGetTest, NotFoundAnywhere) {
    std::vector<HashFunc*> hashFuncs = {
        new MultiHash(1),
        new MultiHash(2),
        new MultiHash(3)
    };
    BloomFilter bf(1000, hashFuncs);
    URLBlacklist ub;

    std::string url = "notfound.com";

    GetCommand cmd(bf, ub);
    std::string result = cmd.execute(url);

    EXPECT_EQ(result, "200 Ok\n\nfalse");

    for (auto* f : hashFuncs) delete f;
}

// ----------------------------
// Test case: Edge case - Empty string
// ----------------------------
TEST(BloomFilterGetTest, EmptyString) {
    std::vector<HashFunc*> hashFuncs = {
        new MultiHash(1),
        new MultiHash(2),
        new MultiHash(3)
    };
    BloomFilter bf(1000, hashFuncs);
    URLBlacklist ub;

    std::string url = "";

    GetCommand cmd(bf, ub);
    std::string result = cmd.execute(url);

    EXPECT_EQ(result, "200 Ok\n\nfalse");

    for (auto* f : hashFuncs) delete f;
}