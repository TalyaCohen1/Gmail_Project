#ifndef MULTISERVER_H
#define MULTISERVER_H
#include "../include/MainLoop.h"

class MultiServer {
private:
    int port;

public:
    explicit MultiServer(int port);
    int getPort() const;
    void run(MainLoop& loop);
};

#endif // MULTISERVER_H
