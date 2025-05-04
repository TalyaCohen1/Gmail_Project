#include <cstdlib>  // atoi
#include <iostream>
#include "TCPServer.h"
#include "MainLoop.h"

int main(int argc, char* argv[]) {
    if (argc != 2) {
        std::cerr << "Usage: ./app <port>" << std::endl;
        return 1;
    }
    MainLoop loop= MainLoop(argv[2]); //ענבר תפתרי את זה הוא צריך לקבל שורה שאיתו הוא יאתחל !

    int port = std::atoi(argv[1]);
    TCPServer server(port, loop);  // Create server on given port
    server.run();            // Start handling client
    return 0;
}
