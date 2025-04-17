#include "ConfigParser.h"
#include <sstream>
#include <iostream>
#include <cctype> 

//function that get a line from the user and return the result
ConfigData ConfigParser::parseLine(const std::string& line){
    ConfigData configData;
    configData.size = 0; // Initialize size to 0
    configData.valid = false; // Initialize valid to false

    std::istringstream iss(line); // Use istringstream to parse the line
    std::string token;
    int count = 0;

    // Read the first token (size of the bloom filter)
    if (iss >> token) {
        try {
            configData.size = std::stoi(token);  
            if (configData.size <= 0) {
                return configData; // Invalid size
            }
        } catch (const std::invalid_argument&) {
            return configData; // Invalid size
        }
    } else {
        return configData; // No size provided
    }

    // Read the rest of the tokens (hash functions)
    while (iss >> token) {
        try {
            int hashFunction = std::stoi(token);
            if (hashFunction < 0) {
                return configData; // Invalid hash function
            }
            configData.hashFunctions.push_back(hashFunc);
            count++;
        } catch (const std::invalid_argument&) {
            return configData; // Invalid hash function
        }
    }

    // Check if at least one hash function is provided
    if (count == 0) {
        configData.valid = false;
        return configData; // No hash functions provided
    }

    configData.valid = true; // Valid configuration
    return configData;
}