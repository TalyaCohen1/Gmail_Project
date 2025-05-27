# Use official GCC image with build tools
FROM gcc:latest

# Install CMake and any required tools
RUN apt-get update && apt-get install -y cmake git

# Set working directory
WORKDIR /usr/src/app

# Copy source code
COPY . .

# Create build directory, build the app
RUN mkdir -p build && cd build && cmake .. && make

# Run the mainApp with arguments (e.g., port 12345)
ENTRYPOINT ["./build/mainApp"]
CMD ["12345", "100", "2", "3"]
