#ifndef ICOMMAND_H
#define ICOMMAND_H

#include <string>

// Abstract base class for all command classes
class ICommand {
public:
    // Pure virtual function for executing a command with the given input
    virtual std::string execute(const std::string& input) = 0;
};

#endif // ICOMMAND_H
