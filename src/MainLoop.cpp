#include "MainLoop.h"
#include "BloomFilter.h"
#include "MultiHash.h"
#include "AddCommand.h"
#include "CheckCommand.h"
#include <iostream>
#include <sstream>
#include <map>
#include <fstream>
#include <filesystem>
#include <regex>
#include <string>
#include <vector>
#include <utility> // for std::pair
#include <stdexcept> // for std::out_of_range
#include <cstdlib> // for std::exit
#include <memory> // for std::unique_ptr

std::vector<HashFunc*> MainLoop::convertToHashFunc(const std::vector<int>& hashIDs) {
    // Convert the hash IDs to HashFunc objects
    std::vector<HashFunc*> funcs;
    for (int id : hashIDs) {
        funcs.push_back(new MultiHash(id)); // Create a new MultiHash object for each ID
    }
    return funcs;
}

MainLoop::MainLoop(){
    // Constructor initializes the bloom filter and URL blacklist

    namespace fs = std::filesystem;
    //create data directory and urlblacklist.txt file if they do not exist
    if (!fs::exists("data")) {
        fs::create_directory("data");
    }
    std::ofstream out("data/urlblacklist.txt", std::ios::trunc);
    if (!out.is_open()) {
        std::cerr << "Failed to create blacklist file." << std::endl;
        exit(1);
    }
    out.close();

    while (true) {
    std::cout << "Enter configuration: ";
    std::getline(std::cin, line);
    
    config = parser.parseLine(line);
    if (config.valid) {
        break; 
    }
    std::cerr << "Invalid configuration. Please try again." << std::endl;
    }
    std::vector<HashFunc*> hashFuncs = convertToHashFunc(config.hashFunc); // Convert the hash IDs to HashFunc objects
    bloomFilter = BloomFilter(config.size, hashFuncs); // Create the bloom filter with the given size and hash functions
}


bool MainLoop::isValidCommand(const std::string& command) {
    // Check if the command is valid (e.g., "1" or "2")
    return command == "1" || command == "2";
}
bool MainLoop::isValidURL(const std::string& url) {
    // Check if the URL is valid using a regex pattern
    regex pattern(R"((https:\/\/www\.|http:\/\/www\.|https:\/\/|http:\/\/)?
        [a-zA-Z0-9]{2,}(\.[a-zA-Z0-9]{2,})(\.[a-zA-Z0-9]{2,})?
        (\/[a-zA-Z0-9\-._~%!$&'()+,;=:@/])?)");
    return regex_match(url,pattern);
}

std::pair<int , std::string> splitCommandAndUrl(const std::string& input) {
    std::istringstream iss(input);
    int command;
    std::string url;

    iss >> command;
    std::getline(iss, url); 

    if (!url.empty() && url[0] == ' ') {
        url.erase(0, 1);
    }
    return {command, url};
}

void MainLoop::run() {
    std::map<std::string, ICommand*> commands;
    commands["1"] = new AddCommand(bloomFilter, realBlacklist);
    commands["2"] = new CheckCommand(bloomFilter, realBlacklist);

    std::string input;
    while (std::getline(std::cin, input)) {
        

        auto [commandNum, url] = splitCommandAndUrl(input);

        if (!isSite(url) || !isValidCommand(commandNum)) {
            continue;
        }

        auto it = commands.find(commandNum);
        if (it != commands.end()) {
            it->second->execute(input);
        } 
    }
    // Clean up dynamically allocated memory
    delete commands["1"];
    delete commands["2"];
}
