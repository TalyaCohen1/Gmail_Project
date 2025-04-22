#include "PersistentManager.h"
#include <fstream>
#include <iostream>
#include <filesystem>

std::string PersistentManager::fullPath(const std::string& filename) const {
    return dataDirectory + "/" + filename;
}

PersistentManager::PersistentManager(const std::string& dataDir) : dataDirectory(dataDir) {
    // Create the directory if it doesn't exist
    if (!std::filesystem::exists(dataDirectory)) {
        std::filesystem::create_directories(dataDirectory);
    }
}

bool PersistentManager::saveURLBlacklist(const Blacklist& blacklist, const std::string& filename) {
    std::ofstream out(fullPath(filename));
    if (!out.is_open()) {
        std::cerr << "Error: Could not open file for writing: " << filename << std::endl;
        return false;
    }

    std::vector<std::string> urls = blacklist.getAll(); 

    for (const std::string& url : urls) {
        out << url << std::endl;
    }

    out.close();
    return true;
}

bool PersistentManager::loadURLBlacklist(Blacklist& blacklist, const std::string& filename) {
    std::ifstream in(fullPath(filename));
    if (!in.is_open()) {
        std::cerr << "Warning: Could not open file for reading: " << filename << std::endl;
        return false;
    }

    std::string url;
    while (std::getline(in, url)) {
        if (!url.empty()) {
            blacklist.add(url);
        }
    }

    in.close();
    return true;
}
