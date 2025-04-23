#ifndef MULTIHASH_H
#define MULTIHASH_H

#include "HashFunc.h"
#include <string>

class MultiHash : public HashFunc {
private:
    int times;

public:
    MultiHash(int times);
    MultiHash();
    ~MultiHash();

    int execute(const std::string& input) override;
};

#endif // MULTIHASH_H
