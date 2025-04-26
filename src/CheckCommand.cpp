#include "CheckCommand.h"

// Constructor
// Initializes the CheckCommand with references to a Bloom filter and a real blacklist
CheckCommand::CheckCommand(BloomFilter& bloom, URLBlacklist& blacklist)
    : bloomFilter(bloom), blacklist(blacklist) 
{
}

/*
 * Executes the check for a given URL.
 * First checks using the Bloom filter (fast but may have false positives),
 * then verifies using the real blacklist.
 * 
 * Prints:
 * - "true true" if the Bloom filter and real blacklist both say the URL is blacklisted.
 * - "true false" if it was a false positive (Bloom filter says yes, but blacklist says no).
 * - "false" if the Bloom filter says the URL is definitely not blacklisted.
 */
void CheckCommand::execute(const std::string& url) {
    if (bloomFilter.possiblyContain(url)) {
        std::cout << "true ";
        if (blacklist.contains(url)) {
            std::cout << "true" << std::endl; 
        } else {
            std::cout << "false" << std::endl;
        }
    } else {
        std::cout << "false" << std::endl;
    }
}
