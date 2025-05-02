#include "App.h"
#include <cstdlib>  // atoi
#include <iostream>

int main(int argc, char* argv[]) {
    if (argc != 2) {
        std::cerr << "Usage: ./app <port>" << std::endl;
        return 1;
    }

    int port = std::atoi(argv[1]);
    App app(port);  // Pass port to App
    app.run();
    return 0;
}
