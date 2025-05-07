#include "MainLoop.h"
#include "BloomFilter.h"
#include "MultiHash.h"
#include "PostCommand.h"
#include "DeleteCommand.h"
#include "CommandParser.h"
#include "GetCommand.h"
#include "BadRequest.h"
#include "ConfigParser.h"
#include "ICommand.h"
#include <iostream>
#include <sstream>
#include <map>
#include <fstream>
#include <filesystem>
#include <regex>
#include <string>
#include <vector>
#include <utility> // for std::pair

using namespace std;

// Converts the list of hash IDs to actual hash function objects
vector<HashFunc*> MainLoop::convertToHashFunc(const vector<int>& hashIDs) {
    vector<HashFunc*> funcs;
    for (int id : hashIDs) {
        funcs.push_back(new MultiHash(id)); // Create a new MultiHash object for each ID
    }
    return funcs;
}

// Constructor
MainLoop::MainLoop(string &line) : bloomFilter(0, {}) {
    this->realBlacklist = URLBlacklist(); // Initialize the real blacklist

    namespace fs = filesystem;

    // Create 'data' directory and 'urlblacklist.txt' file if they don't exist
    if (!fs::exists("data")) {
        fs::create_directory("data");
    }
    ofstream out("data/urlblacklist.txt", ios::app); // Open the file in append mode
    if (!out.is_open()) {
        cerr << "Failed to create blacklist file." << endl;
        exit(1);
    }
    out.close();

    // Create a ConfigParser object
    ConfigParser parser = ConfigParser();

    // Parse the configuration line until it is valid
    parser.parseLine(line);
    while (!parser.isValid()) {
        cout << "Invalid configuration" << endl;
        exit(0); // Exit if the configuration is invalid
    }
    
    // Convert hash IDs to HashFunc objects and create BloomFilter
    std::vector<HashFunc*> hashFuncs = convertToHashFunc(parser.getHashFunc());
    bloomFilter = BloomFilter(parser.getSize(), hashFuncs);
    loadBlacklistToBloomFilter(); // Load the blacklist into the Bloom filter


    // Initialize the command map with command objects
    //map<string, ICommand*> commands;
    commands["POST"] = new PostCommand(bloomFilter,realBlacklist);
    commands["DELETE"] = new DeleteCommand(realBlacklist);
    commands["GET"] = new GetCommand(bloomFilter, realBlacklist);
    commands["BAD"] = new BadRequest();
    
}

MainLoop::~MainLoop() {
    // Clean up command objects
    for (auto& command : commands) {
        delete command.second; // Delete each command object
    }
}

// Load the blacklist from the file and add URLs to the Bloom filter
void MainLoop::loadBlacklistToBloomFilter() {
    std::ifstream blacklistfile("data/urlblacklist.txt");
    if (!blacklistfile.is_open()) {
        cerr << "Failed to open blacklist file for reading." << endl;
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
ICommand* MainLoop::convertToCMD(string command) {
    // Check if the command exists in the map
    auto it = commands.find(command);
    if (it != commands.end()) {
        return it->second; // Return the command object
    }
    return commands["BAD"]; // Return null if command not found
}

// Main loop for handling user input and executing commands
string MainLoop::run(string input) {
    if (input.empty()) {
        return nullptr; // Skip empty lines
    }
    CommandParser parser = CommandParser(input);
    ICommand* cmd = convertToCMD(parser.getCommand());
    if (!cmd) {
        return nullptr; // Skip if command is null
    }
    string response = cmd->execute(parser.getUrl());
    return response; // Return the response from the command
}
