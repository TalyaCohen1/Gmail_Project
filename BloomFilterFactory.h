#include <memory>
#include "BloomFilter.h"
#include "HashFunc.h"

class BloomFilterFactory {
public:
    static std::unique_ptr<BloomFilter> create(int size, const std::vector<int>& hashIds);
};

