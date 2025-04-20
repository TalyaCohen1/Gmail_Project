#pragma once
#include <unordered_set>
#include <string>

class Blacklist {
public:
    void loadFromFile(const std::string& path);  // e.g., "blacklist.txt"
    bool contains(const std::string& url) const;

private:
    std::unordered_set<std::string> urls;
};
