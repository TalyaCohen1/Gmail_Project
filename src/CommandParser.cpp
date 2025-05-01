#include "CommandParser.h"
#include "PostCommand.h"
#include "DeleteCommand.h"
#include "GetCommand.h"
#include "ICommand.h"
#include <regex>
#include <map>
#include <string>
#include "BadRequest.h"


using namespace std;
CommandParser::CommandParser(string& line){
    istringstream iss(line);
    iss >> this.command;
    iss >> this.url;
    this.validCommand = isValidCommand(cmd);
    this.validUrl = isValidUrl(this.url);
}
CommandParser::~CommandParser() {
    // Destructor implementation (if needed)
}

string CommandParser::getCommand() const {
    return command;
}
string CommandParser::getUrl() const {
    return url;
}
bool CommandParser::isValidCommand(){
    if( command == "POST" || command == "DELETE" || command == "GET"){
        return true;
    }
    return false;
}
bool CommandParser::isValidUrl(){
    regex pattern(R"(^(https?:\/\/)?(www\.)?[a-zA-Z0-9\-]+(\.[a-zA-Z0-9]{2,})+(\/.*)?$)");
    return regex_match(this.url, pattern);
}

ICommand* CommandParser::getCommandObject() {
    if (validCommand && validUrl) {
        return commands[command];
    } else {
        return new BadRequestCommand();
    }
}