#ifndef DELETECOMMAND_H
#define DELETECOMMAND_H

#include <string>
#include <iostream>
#include "BloomFilter.h"
#include "URLBlacklist.h"
#include "ICommand.h"

// DeleteCommand class
// Responsible for deleting a URL from the blacklist
class DeleteCommand : public ICommand {
private:
    //BloomFilter& bloomFilter;  // Reference to the Bloom filter
    URLBlacklist& blacklist;   // Reference to the real URL blacklist

public:
    // Constructor
    DeleteCommand(URLBlacklist& blacklist);

   
    std::string execute(const std::string& url) override;
};

#endif // DELETECOMMAND_H
