#ifndef MAINLOOP_H
#define MAINLOOP_H

#include <string>
#include "BloomFilter.h"
#include "ConfigParser.h"
#include "URLBlacklist.h"
#include <regex>

class MainLoop {
private:
    BloomFilter bloomFilter;
    URLBlacklist realBlacklist;
public:
    MainLoop();
    ~MainLoop();
    void run();  //main loop function
    bool isValidCommand(const int command);
    bool isValidURL(const std::string& url);
    std::vector<HashFunc*> convertToHashFunc(const std::vector<int>& hashIDs);
    std::pair<int , std::string> splitCommandAndUrl(const std::string& input);

    BloomFilter& getBloomFilter() { return bloomFilter; } // Getter for bloomFilter
    URLBlacklist& getRealBlacklist() { return realBlacklist; } // Getter for realBlacklist
};
#endif // MAINLOOP_H
