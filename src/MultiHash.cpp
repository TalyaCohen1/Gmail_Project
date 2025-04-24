#include "MultiHash.h"
#include <functional> // std::hash
#include <string>
#include <sstream> // std::to_string
#include <iostream> // std::cout

MultiHash::MultiHash(int times) : times(times) {}
MultiHash::MultiHash() : times(0) {} // Default constructor initializes times to 1

MultiHash::~MultiHash() {}

// The execute function applies the hash function multiple times to the input string
size_t MultiHash::execute(const std::string& input) const{
    size_t temp = std::hash<std::string>{}(input); // Use std::hash to hash the input string
    for(int i = 0; i < times - 1; i++) {
        temp = std::hash<std::string>{}(std::to_string(temp)); // Use std::hash to hash the string representation of temp
    }
    return temp;
}
