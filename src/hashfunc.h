#include <string>

class hashfunc {
public:

    virtual int execute(const std::string& input) = 0; //pure virtual function

    virtual ~hashfunc() = default; // virtual destructor
};
