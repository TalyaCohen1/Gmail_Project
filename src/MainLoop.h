#ifndef MAINLOOP_H
#define MAINLOOP_H

#include <string>
#include "BloomFilter.h"
#include "ConfigParser.h"
#include "URLBlacklist.h"
#include <regex>
#include "ICommand.h"
#include <map>
#include <vector>
#include "HashFunc.h"

class MainLoop {
private:
    BloomFilter bloomFilter;  // Bloom Filter for storing blacklisted URLs
    URLBlacklist realBlacklist;  // Real URL blacklist (possibly persistent storage)
    std::map<std::string, ICommand*> commands;  // Map of command strings to command objects

public:
    MainLoop(std::string &line);  // Constructor: Initializes the main loop, Bloom filter, and blacklist
    ~MainLoop();
    std::string run(std::string input);  // Main loop function that drives the program
    std::vector<HashFunc*> convertToHashFunc(const std::vector<int>& hashIDs);  // Convert hash IDs to hash functions
    void loadBlacklistToBloomFilter();  // Load blacklisted URLs into the Bloom filter
    ICommand* convertToCMD(string command);

    // Getters for Bloom Filter and real blacklist
    BloomFilter& getBloomFilter() { return bloomFilter; }
    URLBlacklist& getRealBlacklist() { return realBlacklist; }
};

#endif // MAINLOOP_H
