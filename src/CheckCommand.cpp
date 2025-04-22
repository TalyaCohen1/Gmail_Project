#include "CheckCommand.h"

CheckCommand::CheckCommand(BloomFilter &bf, URLBlacklist &rb) : bloomFilter(bf), realBlacklist(rb) {}

void CheckCommand::execute(const std::string &input) override
{
    std::string url = input.substr(2);
    bool possibly = bloomFilter.possiblyContain(url);
    std::cout << (possibly ? "true" : "false");

    if (possibly)
    {
        bool actually = realBlacklist.contains(url);
        std::cout << " " << (actually ? "true" : "false");
    }

    std::cout << std::endl;
}
