#include "App.h"

// Constructor for App
// Initializes the application
App::App() {}

// Runs the application
// Creates and starts the main loop
void App::run() {
    MainLoop loop; // Create a MainLoop object
    loop.run();    // Run the MainLoop
}
