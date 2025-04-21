#include <initialize.h>
#include <vector>

BloomFilter Initialize::create(int size, const std::vector<int>& hashIds) {
   std:: vector<hashfunc*> hashFunctions;
    for (int count : hashIds){
        hashFuunctions.push_back(new multihash(count));
    } 
    BloomFilter bloomFilter(size, hashFunctions);
    return bloomFilter;
}