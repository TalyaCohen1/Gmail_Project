#ifndef BADREQUEST_H
#define BADREQUEST_H
#include "ICommand.h"
#include <string>


class BadRequest: public ICommand {
public:
    // Constructor
    BadRequest() = default;
    std::string execute(const std::string& url) override ;
};
#endif // BADREQUEST_H
