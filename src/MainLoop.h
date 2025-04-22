#ifndef MAINLOOP_H
#define MAINLOOP_H
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
#endif // MAINLOOP_H
