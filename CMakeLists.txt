cmake_minimum_required(VERSION 3.10)

project(MyExample)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)


include(FetchContent)
FetchContent_Declare(
  googletest
  URL https://github.com/google/googletest/archive/refs/tags/release-1.11.0.zip
)

# For Windows: Prevent overriding the parent project's compiler/linker settings
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

# Add the library for ConfigParser
add_library(ConfigParser src/ConfigParser.cpp)
target_include_directories(ConfigParser PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/src)

# Find all test files in the tests directory
file(GLOB TEST_SOURCES tests/*.cpp)

# Add the test executable
add_executable(runTests ${TEST_SOURCES})

# Link the test executable with GTest and ConfigParser
target_link_libraries(runTests gtest_main ConfigParser)

# Enable testing and add the tests
enable_testing()
add_test(NAME runTests COMMAND runTests)