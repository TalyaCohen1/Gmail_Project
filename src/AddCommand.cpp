#include "AddCommand.h"
#include <string>

AddCommand::AddCommand(BloomFilter& bf, URLBlacklist& bl)
            : bloomFilter(bf), realBlacklist(bl) {}
    
void AddCommand:: execute(const std::string& input) {
            std::string url = input.substr(2); 
            //URLBlacklist adder(realBlacklist);
            realBlacklist.add(url);
            bloomFilter.add(url);
            realBlacklist.saveToFile("data/blacklist.txt");
    }    