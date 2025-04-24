#include "MultiHash.h"
#include <functional> // std::hash
#include <string>
#include <limits> // std::numeric_limits
#include <cstdint> // For fixed-width integer types

MultiHash::MultiHash(int times) : times(times) {}
MultiHash::MultiHash() : times(0) {} // Default constructor initializes times to 1

MultiHash::~MultiHash() {}

// The execute function applies the hash function multiple times to the input string
size_t MultiHash::execute(const std::string& input){
    size_t temp = HashFunc::execute(input);
    for(int i = 0; i < times - 1; i++) {
        temp = HashFunc::execute(std::to_string(temp));
    }
    return temp;
}
