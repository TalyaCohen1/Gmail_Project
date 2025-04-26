#include "BloomFilter.h"
#include "HashFunc.h"
#include <vector>
#include <string>
#include <fstream>
#include <iostream>

// Constructor
// Initializes the Bloom filter with a specified size and a set of hash functions
BloomFilter::BloomFilter(int size, const std::vector<HashFunc*>& functions)
    : bitArray(size, false), // Initialize bit array with 'size' false values
      arraySize(size),
      hashNum(functions.size()),
      hashFunctions(functions) // Assumes ownership
{
}

// // Destructor
// // Frees dynamically allocated hash functions
// BloomFilter::~BloomFilter() {
//     for (HashFunc* func : hashFunctions) {
//         delete func; // Free each hash function
//     }
//     hashFunctions.clear(); // Clear the vector after deleting
// }

// Adds a URL to the Bloom filter
void BloomFilter::add(const std::string& url) {
    for (int i = 0; i < hashNum; ++i) {
        int hash_result = hashFunctions[i]->execute(url);
        int index = hash_result % arraySize;
        if (index < 0) index += arraySize; // Fix potential negatives

        bitArray[index] = true;
    }
}

// Checks if a URL possibly exists in the Bloom filter
bool BloomFilter::possiblyContain(const std::string& url) const {
    for (int i = 0; i < hashNum; ++i) {
        int hash_result = hashFunctions[i]->execute(url);
        int index = hash_result % arraySize;
        if (index < 0) index += arraySize; // Fix potential negatives
        
        if (!bitArray[index]) {
            return false; // If any bit is not set, URL is definitely not in filter
        }
    }
    return true; // Otherwise, it might be in the filter
}

// Saves the Bloom filter state to a file
void BloomFilter::saveToFile(const std::string& filename) const {
    std::ofstream file(filename);
    if (!file) {
        std::cerr << "Error opening file for writing: " << filename << std::endl;
        return;
    }

    file << arraySize << "\n";
    for (bool bit : bitArray) {
        file << bit << " "; // Save each bit (0 or 1)
    }
    file << "\n";
    file.close();
}

// Loads the Bloom filter state from a file
void BloomFilter::loadFromFile(const std::string& filename) {
    std::ifstream file(filename);
    if (!file) {
        std::cerr << "Error opening file for reading: " << filename << std::endl;
        return;
    }

    // Read the array size
    file >> arraySize;

    // Resize the bit array
    bitArray.resize(arraySize);

    // Read each bit into the bit array
    for (size_t i = 0; i < arraySize; ++i) {
        int temp;
        file >> temp;
        bitArray[i] = static_cast<bool>(temp);
    }

    file.close();
}

// Getter for the size of the bit array
int BloomFilter::getSize() const {
    return arraySize;
}

// Getter for the number of hash functions
int BloomFilter::getHashNum() const {
    return hashNum;
}

// Getter for the bit array
const std::vector<bool>& BloomFilter::getBitArray() const {
    return bitArray;
}

// Getter for a specific hash function
HashFunc* BloomFilter::getHashFunction(int index) const {
    if (index < 0 || index >= hashNum) {
        return nullptr; // Invalid index
    }
    return hashFunctions[index]; // Return the requested hash function
}
