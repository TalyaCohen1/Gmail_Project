#ifndef TCPSERVER_H
#define TCPSERVER_H

#include <string>

class TCPServer {
public:
    TCPServer(int port = 5555);
    void run();
    int getPort() const;
private:
    int port;
};

#endif
