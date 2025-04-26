#ifndef URLBLACKLIST_H
#define URLBLACKLIST_H

#include <string>
#include <vector>

class URLBlacklist {
private:
    std::vector<std::string> blacklist;  // Vector to store the list of blacklisted URLs

public:
    // Constructor: Initializes the blacklist as an empty vector
    URLBlacklist();

    // Adds a URL to the blacklist if it is not already present
    void add(const std::string& url);

    // Checks if a URL is in the blacklist
    bool contains(const std::string& url) const;

    // Saves the blacklist to a file
    void saveToFile(const std::string& filename) const;

    // Loads the blacklist from a file
    void loadFromFile(const std::string& filename);

    // Getter for the blacklist vector, returns a constant reference to the blacklist
    const std::vector<std::string>& getBlacklist() const;
};

#endif // URLBLACKLIST_H
