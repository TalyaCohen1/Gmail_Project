#ifndef HASH_FUNC_H
#define HASH_FUNC_H

#include <string>

// Abstract base class representing a hash function
class HashFunc {
public:
    // Pure virtual function for executing the hash function on the input string
    virtual size_t execute(const std::string& input) const = 0;

    // Virtual destructor to ensure proper cleanup of derived classes
    virtual ~HashFunc() = default;
};

#endif // HASH_FUNC_H
