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

// Main loop for handling user input and executing commands
void MainLoop::run() {
    map<string, ICommand*> commands;
    commands["POST"] = new PostCommand();
    commands["DELETE"] = new DeleteCommand();
    commands["GET"] = new GetCommand();
    
    std::string input;
    while (std::getline(std::cin, input)) {
        if (input.empty()) {
            continue; // Skip empty lines
        }
        CommandParser parser = CommandParser(input);
        ICommand* cmd = parser.getCommandObject();
        cmd->execute(parser.getUrl());
    }
    
    // Clean up command objects
    for (auto& command : commands) {
        delete command.second; // Delete each command object
    }
}
