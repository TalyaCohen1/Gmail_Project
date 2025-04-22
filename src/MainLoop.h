#ifndef MAINLOOP_H
#define MAINLOOP_H

#include <string>
#include "BloomFilter.h"
#include "ConfigParser.h"
#include "URLBlacklist.h"
#include <regex>

class MainLoop {
private:
    BloomFilter* bloomFilter;
    URLBlacklist realBlacklist;
public:
    MainLoop();
    ~MainLoop() {
        delete bloomFilter; // Clean up the dynamically allocated BloomFilter
    }
    void run();  //main loop function
    bool isValidCommand(const std::string& command);
    bool isValidURL(const std::string& url);
    std::vector<HashFunc*> convertToHashFunc(const std::vector<int>& hashIDs);
    vector<std::string> split(const std::string& str, char delimiter);
};
#endif // MAINLOOP_H
