#include "../include/ConfigParser.h"
#include <sstream>
#include <iostream>
#include <cctype>
#include <regex>

ConfigParser::ConfigParser() {
    // Initialize default values
    this->size = 0;         // Size of the bloom filter
    this->valid = false;    // Validity of the configuration
    this->hashFunc = {};    // Empty vector for hash functions
}

// Parses a line containing the configuration for the Bloom filter
void ConfigParser::parseLine(const std::string& line) {
    std::istringstream iss(line);  // Use istringstream to parse the line
    std::string token;
    int count = 0;

    // Read the first token (size of the bloom filter)
    if (iss >> token) {
        if (token.empty() || token[0] < '1') {
            return;  // Invalid size (not a positive integer)
        }        
        try {
            this->size = std::stoi(token);  
            if (this->size <= 0) {
                this->setToStart();
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
        if (token[0] < '0') {
            this->setToStart();
            return;  // Invalid hash function
        }        
        try {
            int hashFunction = std::stoi(token);
            this->hashFunc.push_back(hashFunction);  // Store the hash function
            count++;
        } catch (const std::invalid_argument&) {
            this->setToStart();
            return; // Invalid hash function format
        } catch (const std::out_of_range&) {
            this->setToStart();
            return; // Hash function out of range
        }
    }

    // Check if at least one hash function is provided
    if (count == 0) {
        this->valid = false;
        return;  // No hash functions provided
    }

    this->valid = true;  // Valid configuration
    return;
}

// Getter for the size of the Bloom filter
int ConfigParser::getSize() {
    return this->size;
}

// Getter for the hash functions
std::vector<int> ConfigParser::getHashFunc() {
    return this->hashFunc;
}

// Returns whether the configuration is valid
bool ConfigParser::isValid() {
    return this->valid;
}

// Resets the parser to its initial state
void ConfigParser::setToStart() {
    this->size = 0;         // Reset size
    this->valid = false;    // Mark configuration as invalid
    this->hashFunc.clear(); // Clear the hash functions vector
}
