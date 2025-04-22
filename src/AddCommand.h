#ifndef ADDCOMMAND_H
#define ADDCOMMAND_H

#include <BloomFilter.h>
#include <URLBlacklist.h>
#include <PersistentManager.h>
#include <ICommand.h>

class AddCommand : public ICommand {
    private:
        BloomFilter& bloomFilter;
        URLBlacklist& realBlacklist;
        PersistentManager& pm;
    
    public:
        AddCommand(BloomFilter& bf, URLBlacklist& bl, PersistentManager& p);
           
        void execute(const std::string& input) override;
};
#endif // ADDCOMMAND_H
    