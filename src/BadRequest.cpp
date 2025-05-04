#include "BadRequest.h"

std::string BadRequest::execute(const std::string& url) {
    return "400 Bad Request\n"; // Return a 400 Bad Request response
}