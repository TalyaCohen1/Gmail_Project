#ifndef TCPSERVER_H
#define TCPSERVER_H

#include <string>
#include "MainLoop.h"

class TCPServer {
public:
    TCPServer(int port);
    void run(MainLoop& loop);
    int getPort() const;
private:
    int port;
};

#endif
