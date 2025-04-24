#include "CheckCommand.h"

CheckCommand::CheckCommand(BloomFilter &bloom, URLBlacklist &blacklist)
    : bloomFilter(bloom), blacklist(blacklist) // Use initializer list
{
}

/*
 * Checks if the URL is blacklisted using the bloom filter and the real blacklist.
 * Returns true if blacklisted, false otherwise.
 * Also prints if it was a false positive.
 */
void CheckCommand::execute(const std::string &url)
{
    if (bloomFilter.possiblyContain(url))
    {
        std::cout << "true ";
        if (blacklist.contains(url)){
            std::cout << "true" << std::endl; 
        }
        else{
            std::cout << "false" << std::endl;
        }
    }
    else{
        std::cout << "false" << std::endl;
    }
}

