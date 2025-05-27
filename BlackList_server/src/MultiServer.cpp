#include "../include/MainLoop.h"
#include "../include/MultiServer.h"
#include <iostream>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <cstring>
#include <thread>

using namespace std;

void handleClient(int client_sock, MainLoop& loop) {
    char buffer[4096];
    while (true) {
        memset(buffer, 0, sizeof(buffer));
        int bytes_read = recv(client_sock, buffer, sizeof(buffer) - 1, 0); // Read data from the client
        if (bytes_read <= 0) break;

        string request(buffer);
        string response = loop.run(request);
        send(client_sock, response.c_str(), response.length(), 0); // Send response back to the client
    }

    close(client_sock);
}

MultiServer::MultiServer(int port) : port(port) {}

int MultiServer::getPort() const {
    return port;
}

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

    if (listen(server_sock, 1) < 0) {
        perror("listen");
        close(server_sock);
        return;
    }

    while (true) {
        sockaddr_in client_addr{};
        socklen_t client_len = sizeof(client_addr);
        int client_sock = accept(server_sock, (sockaddr*)&client_addr, &client_len);
        if (client_sock < 0) {
            perror("accept");
            continue;
        }

        thread client_thread(handleClient, client_sock, ref(loop));
        client_thread.detach();
    }

    close(server_sock);
}