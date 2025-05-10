# tests.Dockerfile
FROM gcc:latest

RUN apt-get update && apt-get install -y cmake git

WORKDIR /usr/src/app

COPY . .

# Build the project inside a build directory
RUN mkdir -p build && cd build && cmake .. && make

#run tests
CMD ["./build/runTests"]
