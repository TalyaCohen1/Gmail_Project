#include <fstream> //added for file operations

Initialize::Initialize(size_t size, const std::vector<std::shared_ptr<HashFunc>>& hashFunc){
    // Constructor implementation
    bitArray(size, false);      // Initialize bitArray with size and set all bits to false
    hashFunc(hashFunc);         // Initialize hashFunctions with the provided vector of shared pointers
    arraySize(size);            // Initialize arraySize with the provided size
}

size_t Initialize::getBitIndex(size_t hashValue) const {
    return hashValue % arraySize;
}

void Initialize :: add(const std:: string& url){
    for (const auto& hashFunc : hashFunctions) {
        size_t hashValue = (*hashFunc)(url);
        bitArray[getBitIndex(hashValue)] = true;
    }
}

void Initialize::saveToFile(const std::string& filename) const {
    std::ofstream out(filename, std::ios::binary);
    for (bool bit : bitArray) {
        char value = bit ? 1 : 0;
        out.write(&value, sizeof(char));
    }
}

void Initialize::loadFromFile(const std::string& filename) {
    std::ifstream in(filename, std::ios::binary);
    for (size_t i = 0; i < arraySize; ++i) {
        char value;
        in.read(&value, sizeof(char));
        bitArray[i] = (value != 0);
    }
}

void Initialize::clear() {
    std::fill(bitArray.begin(), bitArray.end(), false); // Set all bits to false
}