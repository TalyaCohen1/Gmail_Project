FROM gcc:latest

# Install cmake and git
RUN apt-get update && apt-get install -y cmake git

# Set working directory
WORKDIR /usr/src/app

# Copy project files into container
COPY . .

# Build the project inside a build directory
RUN mkdir -p build && cd build && cmake .. && make

# Set working directory to where runTests was generated
WORKDIR /usr/src/app/build

# Run the tests
# CMD ["./runTests"]
# No CMD defined

