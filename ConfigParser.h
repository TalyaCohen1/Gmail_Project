#include <string>
#include <vector>

//struct to save the first line from the user
struct ConfigData {
    int size;  //size of the bloomfilter
    std::vector<int> hashFunc;  
    bool valid; 
};

class ConfigParser {
public:
    //function that get a line from the user and return the result
    ConfigData parseLine(const std::string& line);
};
