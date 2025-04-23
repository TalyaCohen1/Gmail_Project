#include "MultiHash.h"
#include <functional> // std::hash
#include <string>

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
    int result = myHasher(current);

    for (int i = 1; i < times; ++i) {
        current = std::to_string(result);
        result = myHasher(current);
    }

    return static_cast<int>(result); 
}
