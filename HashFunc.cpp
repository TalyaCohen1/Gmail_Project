//interface for hash functions
class HashFunc {
    public:
        virtual size_t operator()(const std::string& str) const = 0;
        virtual ~HashFunc() = default;
    };