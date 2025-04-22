#ifndef MANAGER_H
#define MANAGER_H

#include <string>
#include "URLBlacklist.h"
// class for managing persistent data storage
// This class is responsible for saving and loading data to and from files
class PersistentManager {
private:
    std::string dataDirectory;  // Directory for storing data files 
    std::string fullPath(const std::string& filename) const; // Helper function to get the full path of a file
    
public:
    PersistentManager(const std::string& dataDir);
    
    bool saveURLBlacklist(const URLBlacklist& blacklist, const std::string& filename);
    bool loadURLBlacklist(URLBlacklist& blacklist, const std::string& filename);
};

#endif // MANAGER_H