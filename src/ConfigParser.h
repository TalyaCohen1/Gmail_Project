#ifndef CONFIGPARSER_H
#define CONFIGPARSER_H
#include <string>
#include <vector>

class ConfigParser {
//struct to save the first line from the user
private: 
    int size; //size of the bloomfilter
    std::vector<int> hashFunc;  //how many time we will call every hash function
    bool valid;
;

public:
    //function that get a line from the user and return the result
    void parseLine(const std::string& line);
    int getSize();
    std::vector<int> getHashFunc();
    bool isValid();
};
#endif // configparser_h