#ifndef CONFIGPARSER_H
#define CONFIGPARSER_H
#include <string>
#include <vector>

class ConfigParser {

private: 
    int size; //size of the bloomfilter
    std::vector<int> hashFunc;  //how many time we will call every hash function
    bool valid;
;

public:
    ConfigParser(); //constructor
    //function that get a line from the user and return the result
    void parseLine(const std::string& line);
    int getSize();
    std::vector<int> getHashFunc();
    bool isValid();
    void setToStart();
};
#endif // configparser_h