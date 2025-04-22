#ifndef URLCHECKER_H
#define URLCHECKER_H

#include <string>
#include "BloomFilter.h"
#include "URLBlacklist.h"
#include <iostream>

class URLChecker {
public:
    URLChecker(BloomFilter& bloom, URLBlacklist& blacklist);

    // Returns true if blacklisted, false otherwise
    // Also prints if it was a false positive
    void execute(const std::string& url);

private:
    BloomFilter& bloomFilter;
    URLBlacklist& blacklist;
};

#endif