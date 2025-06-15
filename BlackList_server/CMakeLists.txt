cmake_minimum_required(VERSION 3.10)

project(MyExample)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# GoogleTest setup
include(FetchContent)
FetchContent_Declare(
  googletest
  URL https://github.com/google/googletest/archive/refs/tags/release-1.11.0.zip
)
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fsanitize=thread -g")

# Create a library for the BloomFilter system
add_library(BloomLib
    src/BadRequest.cpp
    include/BadRequest.h
    src/BloomFilter.cpp
    include/BloomFilter.h
    src/CommandParser.cpp
    include/CommandParser.h
    src/ConfigParser.cpp
    include/ConfigParser.h
    src/DeleteCommand.cpp
    include/DeleteCommand.h
    src/GetCommand.cpp
    include/GetCommand.h
    include/HashFunc.h
    include/ICommand.h
    src/main.cpp
    src/MainLoop.cpp
    include/MainLoop.h
    src/MultiHash.cpp
    include/MultiHash.h
    src/PostCommand.cpp
    include/PostCommand.h
    src/TCPServer.cpp
    include/TCPServer.h
    src/URLBlacklist.cpp
    include/URLBlacklist.h
    src/MultiServer.cpp
    include/MultiServer.h
    include/ThreadPool.h
    src/ThreadPool.cpp
)
target_include_directories(BloomLib PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/src)

# # Find all test source files in tests/
# file(GLOB TEST_SOURCES tests/*.cpp)

# # Create test executable and link everything
# add_executable(runTests ${TEST_SOURCES})
# target_link_libraries(runTests
#     gtest_main
#     BloomLib
# )

# Main application executable
file(GLOB SRC_FILES src/*.cpp)
add_executable(mainApp ${SRC_FILES})
target_include_directories(mainApp PUBLIC ${CMAKE_CURRENT_SOURCE_DIR}/src)

# enable_testing()
# add_test(NAME runTests COMMAND runTests)
