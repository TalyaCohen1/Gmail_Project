#include "URLBlacklist.h"
#include <string>
#include <vector>
#include <fstream>
#include <filesystem>
#include <iostream>

void URLBlacklist::add(const std::string& url){
    // Check if the URL is already in the blacklist
    if (contains(url)) {
        return; // URL already exists, no need to add it again
    }
    blacklist.push_back(url); // Add the URL to the blacklist
}

bool URLBlacklist::contains(const std::string& url) const{
    // Check if the URL is in the blacklist
    for (const auto& blacklistedUrl : blacklist) {
        if (blacklistedUrl == url) {
            return true; // URL found in the blacklist
        }
    }
    return false; // URL not found in the blacklist
}

void URLBlacklist::saveToFile(const std::string& filename) const{
    std::ofstream outFile(std::filesystem::current_path() / filename); // Open the file for writing
    if (outFile.is_open()) {
        for (const auto& bl_url : blacklist) {
            outFile << bl_url << std::endl;
        }
        outFile.close();
    } else {
        std::cerr << "Unable to open file for writing" << std::endl;
    }
}

void URLBlacklist::loadFromFile(const std::string& filename){
    std::ifstream inFile(std::filesystem::current_path() / filename); // Open the file for reading
    if (inFile.is_open()) {
        std::string url;
        while (std::getline(inFile, url)) {
            add(url); // Add each URL to the blacklist
        }
        inFile.close();
    } else {
        std::cerr << "Unable to open file for reading" << std::endl;
    }
    // If the file doesn't exist, it will be created when saving the blacklist
    // If the file exists, it will be overwritten with the current blacklist
}