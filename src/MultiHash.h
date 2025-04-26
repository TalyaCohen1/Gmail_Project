#ifndef MULTIHASH_H
#define MULTIHASH_H

#include "HashFunc.h"  // Include base class for polymorphic behavior
#include <string>

// MultiHash class: A custom hash function that applies a base hash function multiple times
class MultiHash : public HashFunc {
private:
    int times; // Number of times the hash function is applied

public:
    // Constructor that initializes the number of times the hash function should be applied
    MultiHash(int times);

    // Default constructor, initializes times to 0 (or 1, based on logic)
    MultiHash();

    // Destructor, no dynamic memory allocation, so no specific cleanup needed
    ~MultiHash();

    // Override execute method from the HashFunc base class
    // This applies the base hash function 'times' number of times
    size_t execute(const std::string& input) const override; 
};

#endif // MULTIHASH_H
