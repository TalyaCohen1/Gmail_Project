#ifndef APP_H
#define APP_H
#include <map>
#include <string>
#include <iostream>
#include "ICommand.h"

using namespace std;

class App{
private:
    map<int ,ICommand*> m_commands;
public:
    App(map<int ,ICommand*> commands);
    void run();
};
#endif // APP_H

