#ifndef INTIALIZE_H
#define INTIALIZE_H
#include "BloomFilter.h"
#include "HashFunc.h"
#include "MultiHash.h"
#include "ConfigParser.h"
#include <vector>

class Initialize {
public:
    // Function to create a BloomFilter object with the given size and hash functions
    static BloomFilter create(int size, const std::vector<int>& hashIds);
};

#endif