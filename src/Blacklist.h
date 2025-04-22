#ifndef BLACKLIST_H
#define BLACKLIST_H

#include <unordered_set>
#include <string>

using namespace std;

class Blacklist {
public:
    void loadFromFile(const string& path);  // e.g., "blacklist.txt"
    bool contains(const string& url) const;

private:
    unordered_set<string> urls;
};

#endif
