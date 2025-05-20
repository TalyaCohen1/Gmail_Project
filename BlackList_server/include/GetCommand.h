#ifndef GETCOMMAND_H
#define GETCOMMAND_H

#include <string>
#include <iostream>
#include "BloomFilter.h"
#include "URLBlacklist.h"
#include "ICommand.h"

// GetCommand class
// Responsible for checking if a URL is blacklisted
class GetCommand : public ICommand {
private:
    BloomFilter& bloomFilter;  // Reference to the Bloom filter
    URLBlacklist& blacklist;   // Reference to the real URL blacklist

public:
    // Constructor
    GetCommand(BloomFilter& bloom, URLBlacklist& blacklist);

    /*
     * Executes the get for a given URL.
     * Prints "true true" if in both Bloom filter and blacklist,
     * "true false" if false positive (only in Bloom filter),
     * "false" if not found at all.
     */
    std::string execute(const std::string& url) override;
};

#endif // GETCOMMAND_H
