#include "DeleteCommand.h"

// Constructor
// Initializes the DeleteCommand
DeleteCommand::DeleteCommand(URLBlacklist &blacklist)
    : blacklist(blacklist) // Use initializer list
{
}

// Executes the delete command
void DeleteCommand::execute(const std::string &url)
{
    if (blacklist.contains(url))
    {
        blacklist.deleteURL(url, "data/urlblacklist.txt"); // Delete the URL from the blacklist
        if (!blacklist.contains(url))
        {
            std::cout << "204 No Content" << std::endl; // URL successfully deleted from the blacklist
        }
    }
    else
    {
        std::cout << "404 Not Found" << std::endl; // URL not found in the blacklist
    }
}