#include "URLBlacklist.h"
#include <string>
#include <vector>
#include <fstream>
#include <filesystem>
#include <iostream>
#include <unordered_set>


// Constructor
URLBlacklist::URLBlacklist()
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

// Save
void URLBlacklist::saveToFile(const std::string& filename) const {
    std::filesystem::path path = filename;
    if (!path.parent_path().empty()) {
        std::filesystem::create_directories(path.parent_path());
    }    
    std::unordered_set<std::string> existingUrls;
    std::ifstream inFile(path);
    std::string line;
    while (std::getline(inFile, line)) {
        if (!line.empty()) {
            existingUrls.insert(line);
        }
    }
    inFile.close();

    std::ofstream outFile(path, std::ios::app);
    if (!outFile.is_open()) {
        std::cerr << "Unable to open file for writing: " << path << std::endl;
        return;
    }

    for (const auto& bl_url : blacklist) {
        if (existingUrls.find(bl_url) == existingUrls.end()) {
            outFile << bl_url << std::endl;
        }
    }
    outFile.close();
}

// Load
void URLBlacklist::loadFromFile(const std::string& filename) {
    std::filesystem::path path = filename;

    std::ifstream inFile(path);
    if (inFile.is_open()) {
        std::string url;
        while (std::getline(inFile, url)) {
            add(url);
        }
        inFile.close();
    } else {
        std::cerr << "Unable to open file for reading: " << path << std::endl;
    }
}

const std::vector<std::string>& URLBlacklist::getBlacklist() const {
    return blacklist;
}
