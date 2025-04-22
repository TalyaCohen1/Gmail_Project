#include "BloomFilter.h"
#include "hashfunc.h"
#include <vector>
#include <string>

BloomFilter(int size, const std::vector<HashFunc *> &functions)
{
    : bitArray(std::vector<bool> bitArray(size));  // creates a vector of bools, all initialized to false
    , arraySize(size)
    , hashNum(functions.size())
    , hashFunctions(functions)
    {
        // hash_array =  new int[hashNum];
        //for (int i = 0; i < hashNum; ++i)
        //{
        //    hashFunctions[i] = new HashFunc(hashFunctions[i]->getId(), i+1); // Create a new hash function object
        //}
    }
}

~BloomFilter()
{
    for (int i = 0; i < hashNum; ++i)
    {
        delete hashFunctions[i]; // Delete the hash function object to free memory
    }
    // delete[] hash_array; // If you had allocated an array, delete it here
}

void add(const std::string &url)
{
    //std::hash<std::string> hf;
    std::string val = url;
    for (int i = 0; i < hashNum; ++i) {
        int hash_result = hashFunctions[i]->hash(url); // Call the hash function with the URL
        int index = hash_result % arraySize; // Get the index in the bit array
        bitArray[index] = true; // Set the bit at the index to true

    }
}

bool possiblyContain(const std::string &url) const
{
    for (int i = 0; i < hashNum; ++i) {
        int hash_result = hashFunctions[i]->hash(url); // Call the hash function with the URL
        int index = hash_result % arraySize; // Get the index in the bit array
        if (!bitArray[index]) { // If any bit is false, the URL is definitely not in the filter
            return false;
        }
    }
    return true; // All bits are true, so the URL might be in the filter
}

void saveToFile(const std::string &filename) const
{
    std::ofstream file(filename, std::ios::binary); // Open the file in binary mode
    if (!file) {
        std::cerr << "Error opening file for writing: " << filename << std::endl;
        return;
    }
    //file.write(reinterpret_cast<const char *>(&arraySize), sizeof(arraySize)); // Write the size of the bloom filter
    //file.write(reinterpret_cast<const char *>(&hashNum), sizeof(hashNum)); // Write the number of hash functions
    //file.write(reinterpret_cast<const char *>(bitArray.data()), bitArray.size() * sizeof(bool)); // Write the bit array
}

void loadFromFile(const std::string &filename)
{
    std::ifstream file(filename, std::ios::binary); // Open the file in binary mode 
    if (!file) {
        std::cerr << "Error opening file for reading: " << filename << std::endl;
        return;
    }
    //file.read(reinterpret_cast<char *>(&arraySize), sizeof(arraySize)); // Read the size of the bloom filter
    //file.read(reinterpret_cast<char *>(&hashNum), sizeof(hashNum)); // Read the number of hash functions
    //file.read(reinterpret_cast<char *>(bitArray.data()), bitArray.size() * sizeof(bool)); // Read the bit array
}

int getSize()
{
    return arraySize; // Return the size of the bloom filter
}
