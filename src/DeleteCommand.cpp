#include "DeleteCommand.h"


// Constructor
// Initializes the DeleteCommand
DeleteCommand::DeleteCommand(URLBlacklist &blacklist)
    : blacklist(blacklist) // Use initializer list
{
}

// Executes the delete command
std::string DeleteCommand::execute(const std::string &url)
{
    if (blacklist.contains(url))
    {
        blacklist.deleteURL(url, "data/urlblacklist.txt"); // Delete the URL from the blacklist
        if (!blacklist.contains(url))
        {
            return "204 No Content\n"; // URL successfully deleted from the blacklist
        }
    }
    else
    {
        return "404 Not Found\n"; // URL not found in the blacklist
    }
}