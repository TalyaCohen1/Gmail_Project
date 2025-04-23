#ifndef ICOMMAND_H
#define ICOMMAND_H
#include <string>
class ICommand {
public:
    virtual void execute(const std::string& input) = 0;
};
#endif // ICOMMAND_H
