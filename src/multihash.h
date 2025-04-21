#include "hashfunc.h"
#include <string>

class multihash : public hashfunc{ 
private:
    int time; //how many times the hash function was called
public:
    multihash(int time);
    int execute(const std::string& input) override; //override the pure virtual function
    ~multihash();
};
