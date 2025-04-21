# Base image with build tools and Google Test
FROM ubuntu:22.04

# Install dependencies
RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    git \
    wget \
    libgtest-dev \
    libgmock-dev \
    && rm -rf /var/lib/apt/lists/*

# Build Google Test (comes uncompiled)
RUN cd /usr/src/gtest && cmake . && make && cp *.a /usr/lib

# Set workdir inside container
WORKDIR /usr/src/app

# Copy project into container
COPY . .

# Compile everything using your Makefile
RUN make

# Set default command to run the main app
CMD ["./main"]

# CMD ["./runTests"]
