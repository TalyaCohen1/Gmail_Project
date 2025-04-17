#include "mainloop.h"
#include "initialize.h"
#include "UrlBlacklist.h"
#include "ConfigParser.h"
#include <iostream>

void mainloop :: run(){
    // Create a bloom filter with the given size and hash functions
    std ::string input;
    ConfigParser configParser;
    ConfigData configData;

    std::cout << "Enter the size of the bloom filter and the hash functions (e.g., 100 1 2 3): ";
    std::getline(std::cin, input); // Read the entire line of input
    configData = configParser.parseLine(input); // Parse the input line

    if(!configData.valid) {
        std::cerr << "Invalid input. Please enter a valid size and hash functions." << std::endl;
        return; // Exit if the input is invalid
    }

    // Create the bloom filter using the factory
    BloomFilter bloomFilter =initalize::create(configData.size, configData.hashFunc);
    UrlBlacklist realBlacklist;
    realBlacklist.loadFromFile("data/blacklist.txt");
    bloomFilter->loadFromFile("data/bloom.dat");            // Load the bloom filter from a file


    while (std:: getline(std:: cin, input)){

        if (input.size() < 2) continue;

        if (input.substr(0, 2) == "1 ") {
            std::string url = input.substr(2);
            bloomFilter->add(url);
            realBlacklist.add(url);
            bloomFilter->saveToFile("data/bloom.dat");
            realBlacklist.saveToFile("data/blacklist.txt");
        }
        else if (input.substr(0, 2) == "2 ") {
            std::string url = input.substr(2);
            bool bloomResult = bloomFilter->possiblyContain(url);
            std::cout << (bloomResult ? "true" : "false") << " ";

            if (bloomResult) {
                bool real = realBlacklist.contains(url); 
                std::cout << (real ? "true" : "false");
            }
            std::cout << std::endl;
        }
    }    
}