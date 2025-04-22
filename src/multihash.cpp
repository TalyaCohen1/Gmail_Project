#include "MultiHash.h"
#include <functional> // std::hash
#include <string>

MultiHash::MultiHash(int times) : times(times) {}

MultiHash::~MultiHash() {}

int MultiHash::execute(const std::string& input) const {
    std::string current = input;
    std::hash<std::string> myHasher;
    int result = myHasher(current);

    for (int i = 1; i < times; ++i) {
        current = std::to_string(result);
        result = myHasher(current);
    }

    return static_cast<int>(result); 
}
