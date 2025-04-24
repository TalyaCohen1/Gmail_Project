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
size_t MultiHash::execute(const std::string& input){
    size_t temp = HashFunc::execute(input);
    for(int i = 0; i < times - 1; i++) {
        temp = HashFunc::execute(std::to_string(temp));
    }
    return temp;
}
