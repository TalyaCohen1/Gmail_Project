#include "URLBlacklist.h"
#include <string>
#include <vector>
#include <fstream>
#include <filesystem>
#include <iostream>
#include <unordered_set>

// Constructor: Initializes the blacklist as an empty vector
URLBlacklist::URLBlacklist()
    : blacklist()  // Initialize the blacklist vector
{
}

// Adds a URL to the blacklist if it's not already present
void URLBlacklist::add(const std::string& url) {
    if (contains(url)) {
        return; // URL already exists, no need to add it again
    }
    blacklist.push_back(url); // Add the URL to the blacklist
}

// Checks if a URL is in the blacklist
bool URLBlacklist::contains(const std::string& url) const {
    // Iterate through the blacklist and check if the URL is present
    for (const auto& blacklistedUrl : blacklist) {
        if (blacklistedUrl == url) {
            return true; // URL found in blacklist
        }
    }
    return false; // URL not found in blacklist
}

// Saves the current blacklist to a file
void URLBlacklist::saveToFile(const std::string& filename) const {
    std::filesystem::path path = filename;

    // Ensure the parent directory of the file exists
    if (!path.parent_path().empty()) {
        std::filesystem::create_directories(path.parent_path());
    }

    std::unordered_set<std::string> existingUrls; // Set to store existing URLs in the file
    std::ifstream inFile(path);  // Open the file for reading
    std::string line;
    
    // Read the existing URLs from the file
    while (std::getline(inFile, line)) {
        if (!line.empty()) {
            existingUrls.insert(line);  // Add the URL to the set if it's not empty
        }
    }
    inFile.close();  // Close the input file

    std::ofstream outFile(path, std::ios::app); // Open the file for appending
    if (!outFile.is_open()) {
        std::cerr << "Unable to open file for writing: " << path << std::endl;
        return; // Exit if the file can't be opened
    }

    // Write any new URLs from the blacklist to the file
    for (const auto& bl_url : blacklist) {
        if (existingUrls.find(bl_url) == existingUrls.end()) {
            outFile << bl_url << std::endl; // Append the URL if it doesn't exist in the file
        }
    }
    outFile.close(); // Close the output file
}

// Loads the blacklist from a file
void URLBlacklist::loadFromFile(const std::string& filename) {
    std::filesystem::path path = filename;

    std::ifstream inFile(path); // Open the file for reading
    if (inFile.is_open()) {
        std::string url;
        while (std::getline(inFile, url)) {
            add(url); // Add each URL from the file to the blacklist
        }
        inFile.close(); // Close the file after reading
    } else {
        std::cerr << "Unable to open file for reading: " << path << std::endl;
    }
}

// Returns the current blacklist
const std::vector<std::string>& URLBlacklist::getBlacklist() const {
    return blacklist; // Return a reference to the blacklist
}
