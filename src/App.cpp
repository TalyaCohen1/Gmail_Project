#include "App.h"
#include "TCPServer.h"

App::App(int port) : port(port) {}

void App::run() {
    TCPServer server(port);  // Create server on given port
    server.run();            // Start handling client
}

