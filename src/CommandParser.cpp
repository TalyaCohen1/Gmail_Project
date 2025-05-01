#include "CommandParser.h"
#include "PostCommand.h"
#include "DeleteCommand.h"
#include "GetCommand.h"
#include "ICommand.h"
#include <regex>
#include <map>
#include <string>


using namespace std;
CommandParser::CommandParser(string& line){
    istringstream iss(line);
    iss >> this.command;
    iss >> this.url;
    this.validCommand = isValidCommand(cmd);
    this.validUrl = isValidUrl(this.url);
    commands["POST"] = new PostCommand();
    commands["DELETE"] = new DeleteCommand();
    commands["GET"] = new GetCommand();
}
CommandParser::~CommandParser() {
    for (auto& pair : commands) {
        delete pair.second; // Delete each command object
    }
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

void CommandParser::send_to_command() {
    if (validCommand && validUrl) {
        commands[command]->execute(url);
    } else {
        // שליחה של הודעה מתאימה לשרת
        //ההודעה צריכה להיות -- בדיוק ככה : 400 Bad Request
    }
}
