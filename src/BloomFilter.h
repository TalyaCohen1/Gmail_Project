
#include "hashfunc.h"
#include <vector>
#include <string>

class BloomFilter {
private:
    int size;
    std::vector<bool> bitArray;
    std::vector<HashFunc*> hashFunctions;

public:
    BloomFilter(int size, const std::vector<HashFunc*>& functions);
    ~BloomFilter(); // נשתמש בו כדי למחוק את הפונקציות שהוקצו בזיכרון

    void add(const std::string& url);
    bool possiblyContain(const std::string& url) const;
 
    void saveToFile(const std::string& filename) const;
    void loadFromFile(const std::string& filename);

    int getSize();
};
