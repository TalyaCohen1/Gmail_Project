#include "MainLoop.h"
#include "ConfigParser.h"
#include "Initialize.h"
#include "PersistentManager.h"
#include "URLBlacklist.h"
#include "BloomFilter.h"
#include "AddCommand.h"
#include "CheckCommand.h"
#include "ICommand.h"
#include <map>

#include <iostream>
#include <sstream>

MainLoop::MainLoop(){
    // Constructor initializes the bloom filter and URL blacklist
    pm = PersistentManager("data"); // Initialize the persistent manager

    std::string line;
    std::cout << "Enter configuration";
    std::getline(std::cin, line);

    ConfigParser parser;
    ConfigData config = parser.parseLine(line);

    if (!config.valid) {
        std::cerr << "Invalid configuration. Exiting." << std::endl;
        exit(1);
    }

    bloomFilter = BloomFilter(config.size, config.hashFunc); // Create the bloom filter with the given size and hash functions

    pm.loadURLBlacklist(realBlacklist, "blacklist.txt");
}

void MainLoop::run() {
    std::map<std::string, ICommand*> commands;
    commands["1"] = new AddCommand(bloomFilter, realBlacklist);
    commands["2"] = new CheckCommand(bloomFilter, realBlacklist);

    std::string input;
    while (std::getline(std::cin, input)) {
        if (input.size() < 2) continue;

        std::string commandKey = input.substr(0, 1);
        if (commands.find(commandKey) != commands.end()) {
            commands[commandKey]->execute(input);
        } 
    }

    delete commands["1"];
    delete commands["2"];
}
