#ifndef BLOOMFILTER_H
#define BLOOMFILTER_H

#include "HashFunc.h"
#include <vector>
#include <string>
#include <fstream>
#include <iostream>

class BloomFilter {
public:
    // Constructor: takes ownership of hash functions
    BloomFilter(int size, const std::vector<HashFunc*> &functions);

    // Destructor
    ~BloomFilter()= default; // Default destructor handles cleanup

    // Adds a URL to the filter
    void add(const std::string& url);

    // Checks if a URL is possibly in the filter
    bool possiblyContain(const std::string& url) const;

    // Saves the bit array to a file
    void saveToFile(const std::string& filename) const;

    // Loads the bit array from a file
    void loadFromFile(const std::string& filename);

    // Get the size of the Bloom filter
    int getSize() const;

    // Get the number of hash functions
    int getHashNum() const;

    // Get the bit array (const reference)
    const std::vector<bool>& getBitArray() const;

    // Get the hash function at a specific index
    HashFunc* getHashFunction(int index) const;

private:
    std::vector<bool> bitArray;               // The bit array
    int arraySize;                            // Size of the bit array
    int hashNum;                              // Number of hash functions
    std::vector<HashFunc*> hashFunctions;     // Owned hash functions
};

#endif // BLOOMFILTER_H
