#ifndef ADDCOMMAND_H
#define ADDCOMMAND_H

#include "BloomFilter.h"
#include "URLBlacklist.h"
#include "ICommand.h"

class AddCommand : public ICommand {
    private:
        BloomFilter& bloomFilter;
        URLBlacklist& realBlacklist;
    
    public:
        AddCommand(BloomFilter& bf, URLBlacklist& bl);
           
        void execute(const std::string& input) override;
};
#endif // ADDCOMMAND_H
    