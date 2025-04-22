#ifndef CHECKCOMMAND_H
#define CHECKCOMMAND_H

#include "ICommand.h"
#include "BloomFilter.h"
#include "URLBlacklist.h"
#include <iostream>

class CheckCommand : public ICommand {
private:
    BloomFilter& bloomFilter;
    URLBlacklist& realBlacklist;

public:
    CheckCommand(BloomFilter& bf, URLBlacklist& rb) : bloomFilter(bf), realBlacklist(rb) {}

    void execute(const std::string& input) override {
        std::string url = input.substr(2); // מתחיל אחרי "2 "
        bool possibly = bloomFilter.possiblyContain(url);
        std::cout << (possibly ? "true" : "false");

        if (possibly) {
            bool actually = realBlacklist.contains(url);
            std::cout << " " << (actually ? "true" : "false");
        }

        std::cout << std::endl;
    }
};

#endif
