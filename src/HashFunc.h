#ifndef HASH_FUNC_H
#define HASH_FUNC_H
#include <string>

class HashFunc {
public:

    virtual int execute(const std::string& input) = 0; //pure virtual function

    virtual ~HashFunc() = default; // virtual destructor
};


#endif
