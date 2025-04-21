FROM gcc:latest

# Install CMake and Git
RUN apt-get update && apt-get install -y cmake git

# Create a working directory
WORKDIR /usr/src/app

# Clone GoogleTest into a subdirectory
RUN git clone https://github.com/google/googletest.git

# Copy your local project files into the container
COPY . .

# Build your project
RUN mkdir -p build && cd build && cmake .. && make

# Default command
CMD ["./build/runTests"]
