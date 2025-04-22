#include "URLChecker.h"

URLChecker::URLChecker(BloomFilter& bloom, URLBlacklist& blacklist) {
    bloomFilter = bloom;
    Blacklist = blacklist;
}

/*
 * Checks if the URL is blacklisted using the bloom filter and the real blacklist.
 * Returns true if blacklisted, false otherwise.
 * Also prints if it was a false positive.
*/
void URLChecker::execute(const std::string& url) {
    // Check if the URL is in the bloom filter
    if (bloomFilter.possiblyContain(url)) {
        // If it is, check if it is in the real blacklist
        std::cout << "true ";
        if (Blacklist.contains(url)) {
            std::cout << "true"; // URL is blacklisted
        } else {
            std::cout << "False";
        }
    } else {
        std::cout << "False";
    }
}

