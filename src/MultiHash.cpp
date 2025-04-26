#include "MultiHash.h"
#include <functional>  // For std::hash
#include <string>
#include <sstream>
#include <iostream>

// Constructor that accepts the number of times to apply the hash function
MultiHash::MultiHash(int times) : times(times) {}

// Default constructor: initializes times to 0 (or 1, depending on use case)
MultiHash::MultiHash() : times(0) {}

// Destructor: No dynamic memory allocation, so nothing to clean up here
MultiHash::~MultiHash() {}

// The execute function applies the hash function multiple times to the input string
size_t MultiHash::execute(const std::string& input) const {
    // Apply std::hash to the input string and store the result
    size_t temp = std::hash<std::string>{}(input);

    // Apply the hash function multiple times (times-1 more times)
    for (int i = 0; i < times - 1; i++) {
        // Reapply the hash function to the string representation of the previous result
        temp = std::hash<std::string>{}(std::to_string(temp));
    }

    // Return the final hashed value
    return temp;
}
