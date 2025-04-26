#ifndef CONFIGPARSER_H
#define CONFIGPARSER_H

#include <string>
#include <vector>

class ConfigParser {

private:
    int size;  // Size of the bloom filter
    std::vector<int> hashFunc;  // Vector of hash functions
    bool valid;  // Flag indicating whether the configuration is valid

public:
    // Constructor to initialize the parser
    ConfigParser();

    // Parses a line containing the configuration details
    void parseLine(const std::string& line);

    // Getter for the size of the bloom filter
    int getSize();

    // Getter for the hash functions
    std::vector<int> getHashFunc();

    // Checks if the configuration is valid
    bool isValid();

    // Resets the configuration to its initial state
    void setToStart();
};

#endif // CONFIGPARSER_H
