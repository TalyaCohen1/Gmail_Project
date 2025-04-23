#include "App.h"

App::App() {
    // Constructor initializes the application
    // You can add any necessary initialization code here
}

void App::run() {
    MainLoop loop= MainLoop(); // Create a MainLoop object
    loop.run();
}
