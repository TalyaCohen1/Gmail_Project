#include "ConfigParser.h"
#include <sstream>
#include <iostream>
#include <cctype> 
#include <regex>


ConfigParser::ConfigParser(){
    this->size = 0; // Initialize size to 0
    this->valid = false; // Initialize valid to false
    this->hashFunc = {}; // Initialize hashFunc to an empty vector
}

//function that get a line from the user and return the result
void ConfigParser::parseLine(const std::string& line){
   
    std::istringstream iss(line); // Use istringstream to parse the line
    std::string token;
    int count = 0;


    // Read the first token (size of the bloom filter)
    if (iss >> token) {
        if (token.empty() || token[0] < '1') {
            return; // Invalid size (not a positive integer)
        }        
        try {
            this->size = std::stoi(token);  
            if (this->size <= 0) {
                return; // Invalid size
            }
        } catch (const std::invalid_argument&) {
            return; // Invalid size
        }
    } else {
        return; // No size provided
    }


    // Read the rest of the tokens (hash functions)
    while (iss >> token) {
        if (token.empty() || token[0] < '0') {
            continue; // Skip empty tokens or tokens that are not positive integers
        }        
        try {
            int hashFunction = std::stoi(token);
            this->hashFunc.push_back(hashFunction); // Store the hash function
            count++;
        } catch (const std::invalid_argument&) {
            return; // Invalid hash function
        }
    }

    // Check if at least one hash function is provided
    if (count == 0) {
        this->valid = false;
        return; // No hash functions provided
    }

    this->valid = true; // Valid configuration
    return;
}
int ConfigParser::getSize(){
    return this->size; // Return the size of the bloom filter
}
std::vector<int> ConfigParser::getHashFunc(){
    return this->hashFunc; // Return the hash functions
}
bool ConfigParser::isValid(){
    return this->valid; // Return whether the configuration is valid
}
