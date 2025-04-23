#include "MultiHash.h"
#include <functional> // std::hash
#include <string>
#include <limits> // std::numeric_limits
#include <cstdint> // For fixed-width integer types

MultiHash::MultiHash(int times) : times(times) {}
MultiHash::MultiHash() : times(0) {} // Default constructor initializes times to 1

MultiHash::~MultiHash() {}

/*
This function applies each hash function to the input string to compute
 * multiple hash values. Each value is then mapped to an index in the bit array
 * using modulo operation. The bits at the resulting indices are set to true.
*/
int MultiHash::execute(const std::string& input){
    std::string current = input;
    std::hash<std::string> myHasher;
    size_t result = myHasher(current);  // size_t instead of int

for (int i = 1; i < times; ++i) {
    current = std::to_string(result);
    result = myHasher(current);
}
return static_cast<int>(result % std::numeric_limits<int>::max()); // Clamp to positive range
return static_cast<int>(result % INT32_MAX); // Clamp to positive range

}
