#include "../include/MainLoop.h"
#include "../include/MultiServer.h"
#include "../include/ThreadPool.h"
#include <iostream>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <cstring>
#include <thread>

using namespace std;

// // Function to handle communication with a client
// void handleClient(int client_sock, MainLoop& loop) {
//     char buffer[4096];
//     while (true) {
//         memset(buffer, 0, sizeof(buffer));
//         //read data from the client socket
//         int bytes_read = recv(client_sock, buffer, sizeof(buffer) - 1, 0); // Read data from the client
//         if (bytes_read <= 0) break;

//         // Process request and generate response using the MainLoop logic
//         string request(buffer);
//         string response = loop.run(request);
//         send(client_sock, response.c_str(), response.length(), 0); // Send response back to the client
//     }

//     close(client_sock); // Close the client socket when done
// }

MultiServer::MultiServer(int port) : port(port) {}

int MultiServer::getPort() const {
    return port;
}

// Function to run the multi-threaded server
//Starts the server and listens for incoming client connections.
void MultiServer::run(MainLoop& loop) {

    int server_sock = socket(AF_INET, SOCK_STREAM, 0);
    if (server_sock < 0) {
        perror("socket");
        return;
    }

    sockaddr_in server_addr{};
    server_addr.sin_family = AF_INET;
    server_addr.sin_addr.s_addr = INADDR_ANY;
    server_addr.sin_port = htons(port);

    if (bind(server_sock, (sockaddr*)&server_addr, sizeof(server_addr)) < 0) {
        perror("bind");
        close(server_sock);
        return;
    }

    // Set the server to listen for incoming connections
    if (listen(server_sock, 100) < 0) { 
        perror("listen");
        close(server_sock);
        return;
    }
    ThreadPool pool(4, loop); // Create a thread pool with 4 worker threads


    //accept incoming connections in a loop
    while (true) {
        sockaddr_in client_addr{};
        socklen_t client_len = sizeof(client_addr);
        int client_sock = accept(server_sock, (sockaddr*)&client_addr, &client_len);
        if (client_sock < 0) {
            perror("accept");
            continue;
        }

        pool.addTask(client_sock); // Add the client socket to the thread pool for processing
    }

    close(server_sock);
}