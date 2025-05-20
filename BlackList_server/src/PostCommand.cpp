#include "../include/PostCommand.h"
#include <string>

using namespace std;

// Constructor for PostCommand
// Initializes the command with references to the Bloom filter and the real blacklist
PostCommand::PostCommand(BloomFilter& bf, URLBlacklist& bl)
    : bloomFilter(bf), realBlacklist(bl) {}

// Executes the add command
// Adds the given URL to both the real blacklist and the Bloom filter,
// then saves the updated blacklist to the file
string PostCommand::execute(const string& input) {
    std::string url = input;

    realBlacklist.add(url);            // Add the URL to the real blacklist
    bloomFilter.add(url);              // Add the URL to the Bloom filter
    realBlacklist.saveToFile("data/urlblacklist.txt"); // Save the blacklist to a file
    if(realBlacklist.contains(url) && bloomFilter.possiblyContain(url)) {
        return "201 Created\n"; // Indicate that the URL was successfully added
    }
    else {
        return "500 Internal Server Error\n"; // Indicate an error occurred
    }
}