
#ifndef URLBLACKLIST_H
#define URLBLACKLIST_H

#include <string>
#include <vector>

class URLBlacklist {
private:
    std::vector<std::string> blacklist;

public:
    void add(const std::string& url);
    bool contains(const std::string& url) const;

    void saveToFile(const std::string& filename) const;
    void loadFromFile(const std::string& filename);
};

#endif