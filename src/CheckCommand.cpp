#include "CheckCommand.h"

CheckCommand::CheckCommand(BloomFilter& bloom, URLBlacklist& blacklist)
    : bloomFilter(bloom), blacklist(blacklist) // Use initializer list
{
}

/*
 * Checks if the URL is blacklisted using the bloom filter and the real blacklist.
 * Returns true if blacklisted, false otherwise.
 * Also prints if it was a false positive.
*/
void CheckCommand::execute(const std::string& url) {
    // Check if the URL is in the bloom filter
    if (bloomFilter.possiblyContain(url)) {
        // If it is, check if it is in the real blacklist
        std::cout << "true ";
        if (blacklist.contains(url)) {
            std::cout << "true"; // URL is blacklisted
        } else {
            std::cout << "False";
        }
    } else {
        std::cout << "False";
    }
}

