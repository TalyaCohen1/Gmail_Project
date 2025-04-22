#include "MainLoop.h"
#include "ConfigParser.h"
#include "Initialize.h"
#include "PersistentManager.h"

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

    bloomFilter = Initialize::create(config.size, config.hashFunc); // Create the bloom filter using the factory

    pm.loadURLBlacklist(realBlacklist, "blacklist.txt");


}

void Mainloop :: run(){
    // Main loop for processing user input
    std::string input;

    while (std:: getline(std:: cin, input)){

        if (input.size() < 2) continue;

        if (input.substr(0, 2) == "1 ") {
            UrlBlackList adder(bloomFilter, realBlacklist);
            adder.addUrl(url);
            adder.saveChanges("data/bloom.txt", "data/blacklist.txt");
        }
        else if (input.substr(0, 2) == "2 ") {
            bool possibly = bloomFilter.possiblyContains(url);
            std::cout << (possibly ? "true" : "false") << " ";

            if (possibly) {
                bool actually = realBlacklist.contains(url);
                std::cout << (actually ? "true" : "false");
            }

            std::cout << std::endl;
        }
    }    
}
