class ICommand {
public:
    virtual void execute(const std::string& input) = 0;
};
