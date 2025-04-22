#include "URLBlacklist.h"
#include <string>
#include <vector>
#include <fstream>
#include <filesystem>
#include <iostream>

// Constructor
URLBlacklist::URLBlacklist( )
    : blacklist()  // Initialize the blacklist vector
{
}

void URLBlacklist::add(const std::string& url) {
    if (contains(url)) {
        return; // URL already exists, no need to add it again
    }
    blacklist.push_back(url);
}

bool URLBlacklist::contains(const std::string& url) const {
    for (const auto& blacklistedUrl : blacklist) {
        if (blacklistedUrl == url) {
            return true;
        }
    }
    return false;
}

void URLBlacklist::saveToFile(const std::string& filename) const {
    std::ofstream outFile(std::filesystem::current_path() / filename);
    if (outFile.is_open()) {
        for (const auto& bl_url : blacklist) {
            outFile << bl_url << std::endl;
        }
        outFile.close();
    } else {
        std::cerr << "Unable to open file for writing" << std::endl;
    }
}

void URLBlacklist::loadFromFile(const std::string& filename) {
    std::ifstream inFile(std::filesystem::current_path() / filename);
    if (inFile.is_open()) {
        std::string url;
        while (std::getline(inFile, url)) {
            add(url);
        }
        inFile.close();
    } else {
        std::cerr << "Unable to open file for reading" << std::endl;
    }
}

const std::vector<std::string>& URLBlacklist::getBlacklist() const {
    return blacklist;
}
