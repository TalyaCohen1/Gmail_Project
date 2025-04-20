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
BIN = main
TEST_BIN = runTests

all: $(BIN) $(TEST_BIN)

$(BIN): $(SRCS)
	$(CXX) $(CXXFLAGS) $^ -o $@

$(TEST_BIN): $(SRCS) $(TEST_SRCS)
	$(CXX) $(CXXFLAGS) $^ -o $@ $(GTEST_FLAGS)

clean:
	rm -f $(BIN) $(TEST_BIN)