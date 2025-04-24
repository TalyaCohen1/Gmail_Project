#include "App.h"

App::App() {
}

void App::run() {
    MainLoop loop= MainLoop(); // Create a MainLoop object
    loop.run();
}
