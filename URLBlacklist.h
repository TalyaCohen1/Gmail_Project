#include <string>
#include <vector>

class UrlBlacklist {
private:
    std::vector<std::string> blacklist;

public:
    void add(const std::string& url);
    bool contains(const std::string& url) const;

    void saveToFile(const std::string& filename) const;
    void loadFromFile(const std::string& filename);
};
