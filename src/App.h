#ifndef APP_H
#define APP_H
#include <map>
#include <string>
#include <iostream>
#include <string>
#include "ICommand.h"

using namespace std;

class App{
private:
    map<string ,ICommand*> m_commands;
public:
    App(map<string ,ICommand*> commands);
    void run();
};
#endif // APP_H

