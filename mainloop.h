#include <string>
#include "BloomFilter.h"
#include "ConfigParser.h"

class MainLoop {
private:
    BloomFilter bloomFilter;
    URLBlacklist realBlacklist;
    PersistentManager pm;
public:
    MainLoop();
    void run();  //main loop function
};