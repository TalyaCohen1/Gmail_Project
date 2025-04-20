# Compiler and flags
CXX = g++
CXXFLAGS = -std=c++17 -Wall -I./src

# Google Test libraries
GTEST_FLAGS = -lgtest -lgtest_main -pthread

# Source files
SRCS = $(wildcard src/*.cpp)
TEST_SRCS = $(wildcard tests/*_test.cpp)

# Object files
OBJS = $(SRCS:.cpp=.o)

# Output
TEST_BIN = runTests

# Default target
all: $(TEST_BIN)

# Link the test binary
$(TEST_BIN): $(SRCS) $(TEST_SRCS)
	$(CXX) $(CXXFLAGS) $^ -o $@ $(GTEST_FLAGS)

# Clean
clean:
	rm -f $(TEST_BIN) *.o src/*.o
