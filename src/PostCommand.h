#ifndef POSTCOMMAND_H
#define POSTCOMMAND_H

#include "BloomFilter.h"
#include "URLBlacklist.h"
#include "ICommand.h"

// PostCommand class
// Responsible for adding a URL to both the Bloom filter and the real blacklist
class PostCommand : public ICommand {
private:
    BloomFilter& bloomFilter;     // Reference to the Bloom filter
    URLBlacklist& realBlacklist;  // Reference to the real blacklist

public:
    // Constructor
    // Initializes the AddCommand with references to the Bloom filter and real blacklist
    PostCommand(BloomFilter& bf, URLBlacklist& bl);

    // Executes the add operation:
    // Adds a URL to both the real blacklist and the Bloom filter
    void execute(const std::string& input) override;
};

#endif // POSTCOMMAND_H