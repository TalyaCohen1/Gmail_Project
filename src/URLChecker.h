#pragma once
#include <string>
#include "BloomFilter.h"
#include "Blacklist.h"

class URLChecker {
public:
    URLChecker(BloomFilter& bloom, Blacklist& blacklist);

    // Returns true if blacklisted, false otherwise. Also prints if it was a false positive.
    bool checkURL(const std::string& url);

private:
    BloomFilter& bloomFilter;
    Blacklist& realBlacklist;
};