#ifndef COMMANDPARSER_H
#define COMMANDPARSER_H

#include <string>
#include "ICommand.h" 
#include <map>

using namespace std;
class CommandParser {
private:
    string command;
    string url;
    bool validCommand;
    bool validUrl;

public:
    CommandParser(const string& line);
    ~CommandParser();
    
    string getCommand() const;
    string getUrl() const;
    bool isValidCommand( string& command);
    bool isValidUrl( string& url);
    // ICommand* getCommandObject();
};

#endif 
