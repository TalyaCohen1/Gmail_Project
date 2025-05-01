#ifndef MAINLOOP_H
#define MAINLOOP_H

#include <string>
#include "BloomFilter.h"
#include "ConfigParser.h"
#include "URLBlacklist.h"
#include <regex>
#include <vector>
#include "HashFunc.h"

class MainLoop {
private:
    BloomFilter bloomFilter;  // Bloom Filter for storing blacklisted URLs
    URLBlacklist realBlacklist;  // Real URL blacklist (possibly persistent storage)

public:
    MainLoop();  // Constructor: Initializes the main loop, Bloom filter, and blacklist
    ~MainLoop() = default;  // Default destructor
    void run();  // Main loop function that drives the program
    std::vector<HashFunc*> convertToHashFunc(const std::vector<int>& hashIDs);  // Convert hash IDs to hash functions
    void loadBlacklistToBloomFilter();  // Load blacklisted URLs into the Bloom filter

    // Getters for Bloom Filter and real blacklist
    BloomFilter& getBloomFilter() { return bloomFilter; }
    URLBlacklist& getRealBlacklist() { return realBlacklist; }
};

#endif // MAINLOOP_H
