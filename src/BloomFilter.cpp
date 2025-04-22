#include "BloomFilter.h"
#include "hashfunc.h"
#include <vector>
#include <string>
#include <fstream>
#include <iostream>

// Constructor
BloomFilter::BloomFilter(int size, const std::vector<HashFunc*>& functions)
    : bitArray(size, false),  // Initialize bit array with 'size' false values
      arraySize(size),
      hashNum(functions.size()),
      hashFunctions(functions) // Assumes ownership
{
}

// Destructor
BloomFilter::~BloomFilter() {
    for (int i = 0; i < hashNum; ++i) {
        delete hashFunctions[i]; // Free each hash function
    }
}

// Add a URL
void BloomFilter::add(const std::string& url) {
    for (int i = 0; i < hashNum; ++i) {
        int hash_result = hashFunctions[i]->hash(url);
        int index = hash_result % arraySize;
        bitArray[index] = true;
    }
}

// Check if URL possibly exists
bool BloomFilter::possiblyContain(const std::string& url) const {
    for (int i = 0; i < hashNum; ++i) {
        int hash_result = hashFunctions[i]->hash(url);
        int index = hash_result % arraySize;
        if (!bitArray[index]) {
            return false;
        }
    }
    return true;
}

// Save Bloom filter state to file
void BloomFilter::saveToFile(const std::string& filename) const {
    std::ofstream file(filename, std::ios::binary);
    if (!file) {
        std::cerr << "Error opening file for writing: " << filename << std::endl;
        return;
    }

    // You can implement writing here if needed
}

// Load Bloom filter state from file
void BloomFilter::loadFromFile(const std::string& filename) {
    std::ifstream file(filename, std::ios::binary);
    if (!file) {
        std::cerr << "Error opening file for reading: " << filename << std::endl;
        return;
    }

    // You can implement reading here if needed
}

// Getter for size
int BloomFilter::getSize() const {
    return arraySize;
}

// Getter for number of hash functions
int BloomFilter::getHashNum() const {
    return hashNum;
}

// Getter for bit array
const std::vector<bool>& BloomFilter::getBitArray() const {
    return bitArray;
}

// Getter for a specific hash function
HashFunc* BloomFilter::getHashFunction(int index) const {
    if (index >= 0 && index < hashNum) {
        return hashFunctions[index];
    }
    return nullptr;
}
