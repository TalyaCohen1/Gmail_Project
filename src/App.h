#ifndef APP_H
#define APP_H

class App {
public:
    App(int port);  // constructor with port
    void run();     // runs the TCP server
private:
    int port;
};

#endif
