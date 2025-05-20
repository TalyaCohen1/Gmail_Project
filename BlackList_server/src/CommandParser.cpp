#include "../include/CommandParser.h"
#include "../include/BadRequest.h"
#include "../include/DeleteCommand.h"
#include "../include/GetCommand.h"
#include "../include/ICommand.h"
#include <regex>
#include <map>
#include <string>


using namespace std;
CommandParser::CommandParser(const string& line){
    istringstream iss(line);
    iss >> this->command;
    iss >> this->url;
    validCommand = isValidCommand();
    validUrl = isValidUrl();
}
CommandParser::~CommandParser() {
    // Destructor implementation (if needed)
}

string CommandParser::getCommand() const{
    return this->command;
}
string CommandParser::getUrl() const{
    return this->url;
}
bool CommandParser::isValidCommand(){
    if(command == "POST" || command == "DELETE" || command == "GET"){
        return true;
    }
    return false;
}
bool CommandParser::isValidUrl(){
    regex pattern(R"(^(https?:\/\/)?(www\.)?[a-zA-Z0-9\-]+(\.[a-zA-Z0-9]{2,})+(\/.*)?$)");
    return regex_match(this->url, pattern);
}

// ICommand* CommandParser::getCommandObject() {
//     if (validCommand && validUrl) {
//         return commands[command];
//     } else {
//         return new BadRequestCommand();
//     }
// }