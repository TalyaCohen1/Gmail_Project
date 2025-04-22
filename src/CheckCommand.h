#ifndef CHECKCOMMAND_H
#define CHECKCOMMAND_H

#include <string>
#include "BloomFilter.h"
#include "URLBlacklist.h"
#include <iostream>
#include "ICommand.h"

class CheckCommand :public ICommand {
    private:
    BloomFilter& bloomFilter;
    URLBlacklist& blacklist;
public:
    CheckCommand(BloomFilter& bloom, URLBlacklist& blacklist);

    // Returns true if blacklisted, false otherwise
    // Also prints if it was a false positive
    void execute(const std::string& url);
};
#endif