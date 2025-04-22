#include "AddCommand.h"

AddCommand::AddCommand(BloomFilter& bf, URLBlacklist& bl)
            : bloomFilter(bf), realBlacklist(bl) {}
    
void AddCommand:: execute(const std::string& input) {
            std::string url = input.substr(2); 
            URLBlacklist adder( realBlacklist);
            adder.add(url);
            adder.saveToFile("data/blacklist.txt");
    }    