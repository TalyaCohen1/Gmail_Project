#include "AddCommand.h"

AddCommand::AddCommand(BloomFilter& bf, URLBlacklist& bl, PersistentManager& p)
            : bloomFilter(bf), realBlacklist(bl), pm(p) {}
    
void AddCommand:: execute(const std::string& input) {
            std::string url = input.substr(2); 
            URLBlacklist adder( realBlacklist);
            adder.add(url);
            adder.saveToFile("data/blacklist.txt");
    }    