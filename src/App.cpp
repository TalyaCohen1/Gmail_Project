#include "App.h"

using namespace std;

App::App(map<string ,ICommand*> commands){
    m_commands = commands;
}
void App::run(){
    int commandNum;
    while (true) {
        cout << "Enter command: ";
        cin >> commandNum;
        try{
            m_commands[commandNum]->execute();
        } catch(const std::out_of_range& e) {
            cout << "Invalid command. Please try again." << endl;
        } 
    }
}

