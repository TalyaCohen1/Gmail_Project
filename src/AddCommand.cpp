#include "AddCommand.h"
#include <string>

// Constructor for AddCommand
// Initializes the command with references to the Bloom filter and the real blacklist
AddCommand::AddCommand(BloomFilter& bf, URLBlacklist& bl)
    : bloomFilter(bf), realBlacklist(bl) {}

// Executes the add command
// Adds the given URL to both the real blacklist and the Bloom filter,
// then saves the updated blacklist to the file
void AddCommand::execute(const std::string& input) {
    std::string url = input;

    realBlacklist.add(url);            // Add the URL to the real blacklist
    bloomFilter.add(url);              // Add the URL to the Bloom filter
    realBlacklist.saveToFile("data/urlblacklist.txt"); // Save the blacklist to a file
}
