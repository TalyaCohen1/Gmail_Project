#include <cstdlib>  // atoi
#include <iostream>
#include "../include/TCPServer.h"
#include "../include/MainLoop.h"
using namespace std;
int main(int argc, char* argv[]) {
    if (argc < 4) {
        return 1;
    }
    std::string combinedArgs;
    for (int i = 2; i < argc; ++i) {
        combinedArgs += argv[i];
        if (i != argc - 1)
            combinedArgs += " ";
    }

    MainLoop loop= MainLoop(combinedArgs);
    
    int port = std::atoi(argv[1]);
    TCPServer server(port);  // Create server on given port
    server.run(loop);            // Start handling client
    return 0;
}
