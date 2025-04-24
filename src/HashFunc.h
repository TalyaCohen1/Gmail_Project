#ifndef HASH_FUNC_H
#define HASH_FUNC_H
#include <string>

class HashFunc {
public:
    virtual size_t execute(const std::string& input) const = 0;  // pure virtual
    virtual ~HashFunc() = default; // virtual destructor
};

#endif
