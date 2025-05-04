#ifndef TCPSERVER_H
#define TCPSERVER_H

#include <string>

class TCPServer {
public:
    TCPServer(int port);
    void run();
    int getPort() const;
private:
    int port;
};

#endif
