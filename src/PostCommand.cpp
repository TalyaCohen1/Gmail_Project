#include "PostCommand.h"
#include <string>

// Constructor for PostCommand
// Initializes the command with references to the Bloom filter and the real blacklist
PostCommand::PostCommand(BloomFilter& bf, URLBlacklist& bl)
    : bloomFilter(bf), realBlacklist(bl) {}

// Executes the add command
// Adds the given URL to both the real blacklist and the Bloom filter,
// then saves the updated blacklist to the file
void PostCommand::execute(const std::string& input) {
    std::string url = input;

    realBlacklist.add(url);            // Add the URL to the real blacklist
    bloomFilter.add(url);              // Add the URL to the Bloom filter
    realBlacklist.saveToFile("data/urlblacklist.txt"); // Save the blacklist to a file
    if(realBlacklist.contains(url) && bloomFilter.possiblyContain(url)) {
        std::cout << "201 Created" << std::endl; // Indicate that the URL was successfully added
    }
    else {
        std::cout << "500 Internal Server Error" << std::endl; // Indicate an error occurred
    }
}