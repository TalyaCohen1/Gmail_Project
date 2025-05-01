#include "MainLoop.h"
#include "BloomFilter.h"
#include "MultiHash.h"
#include "AddCommand.h"
#include "CheckCommand.h"
#include "ConfigParser.h"
#include <iostream>
#include <sstream>
#include <map>
#include <fstream>
#include <filesystem>
#include <regex>
#include <string>
#include <vector>
#include <utility> // for std::pair

// Converts the list of hash IDs to actual hash function objects
std::vector<HashFunc*> MainLoop::convertToHashFunc(const std::vector<int>& hashIDs) {
    std::vector<HashFunc*> funcs;
    for (int id : hashIDs) {
        funcs.push_back(new MultiHash(id)); // Create a new MultiHash object for each ID
    }
    return funcs;
}

// Constructor
MainLoop::MainLoop() : bloomFilter(0, {}) {
    namespace fs = std::filesystem;

    // Create 'data' directory and 'urlblacklist.txt' file if they don't exist
    if (!fs::exists("data")) {
        fs::create_directory("data");
    }
    std::ofstream out("data/urlblacklist.txt", std::ios::app); // Open the file in append mode
    if (!out.is_open()) {
        std::cerr << "Failed to create blacklist file." << std::endl;
        exit(1);
    }
    out.close();

    // Create a ConfigParser object
    ConfigParser parser = ConfigParser();
    std::string line;

    // Parse the configuration line until it is valid
    while (true) {
        std::getline(std::cin, line);
        parser.parseLine(line);
        if (parser.isValid()) {
            break; 
        }
    }

    // Convert hash IDs to HashFunc objects and create BloomFilter
    std::vector<HashFunc*> hashFuncs = convertToHashFunc(parser.getHashFunc());
    bloomFilter = BloomFilter(parser.getSize(), hashFuncs);
    loadBlacklistToBloomFilter(); // Load the blacklist into the Bloom filter
}

// Load the blacklist from the file and add URLs to the Bloom filter
void MainLoop::loadBlacklistToBloomFilter() {
    std::ifstream blacklistfile("data/urlblacklist.txt");
    if (!blacklistfile.is_open()) {
        std::cerr << "Failed to open blacklist file for reading." << std::endl;
        return;
    }

    std::string url;
    while (std::getline(blacklistfile, url)) {
        if (!url.empty()) {
            this->bloomFilter.add(url); // Add URL to Bloom filter
            this->realBlacklist.add(url); // Add URL to real blacklist
        }
    }
    blacklistfile.close();
}

// Validate if the given command is valid (1 or 2)
bool MainLoop::isValidCommand(const int command) {
    return command == 1 || command == 2;
}

// Validate the URL format using regex
bool MainLoop::isValidURL(const std::string& url) {
    std::regex pattern(R"(^(https?:\/\/)?(www\.)?[a-zA-Z0-9\-]+(\.[a-zA-Z0-9]{2,})+(\/.*)?$)");
    return regex_match(url, pattern);
}

// Split the input into command number and URL
std::pair<int, std::string> MainLoop::splitCommandAndUrl(const std::string& input) {
    std::istringstream iss(input);
    int command;
    std::string url;

    iss >> command;
    std::getline(iss, url);

    if (!url.empty() && url[0] == ' ') {
        url.erase(0, 1); // Trim leading space if present
    }
    return {command, url};
}

// Main loop for handling user input and executing commands
void MainLoop::run() {
    
    std::string input;
    while (std::getline(std::cin, input)) {
        if (input.empty()) {
            continue; // Skip empty lines
        }
        CommandParser parser = CommandParser(input);
        parser.send_to_command();
        parser.~CommandParser(); // Clean up the parser object
    }
    
}
