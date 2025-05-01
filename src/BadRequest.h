#ifndef "BADREQUEST_H"
#define "BADREQUEST_H"
#include "ICommand.h"
#include <string>


class BadRequestCommand : public ICommand {
public:
    void execute(const std::string& url) override ;
};
#endif // BADREQUEST_H
