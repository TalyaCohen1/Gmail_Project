# first_task - URL Blacklist Filter
This application uses a Bloom filter to efficiently maintain and check against a blacklist of URLs.

## Description
The URL Blacklist Filter allows you to:

1. Add URLs to a blacklist
2. Check if a URL is potentially in the blacklist

It uses a Bloom filter for fast membership testing with a small memory footprint, while maintaining a full list for verification.

## Requirements

- Docker

## Getting Started
## Building and Running with Docker

1. Build the Docker image:
```
docker build -t bloom-filter-app .
```
2. Create a persistent volume for data storage:
```
docker volume create bloom-filter-data
```
3. Run the container:
```
docker run --rm -it -v bloomfilter-data:/usr/src/app/build/data bloomfilter-app ./mainApp
```

## Usage
## Initial Configuration
When you first run the program, you need to provide configuration parameters:
```
<size> <hash1> <hash2> ... <hashN>
```
Where:

- `<size>` is the size of the Bloom filter
- `<hash1> <hash2> ... <hashN>` are the number of times each hash function should be applied

Example:
```
100 1 2 3
```
This creates a Bloom filter of size 100, applying hash function 1 once, hash function 2 twice, and hash function 3 three times.

## Commands
The program accepts two types of commands:

1. Add a URL to the blacklist:
```
1 <url>
```

2. Check if a URL is in the blacklist:
```
2 <url>
```

## Command Outputs

- For command `1` (Add URL): No output is displayed after adding a URL
- For command `2` (Check URL): You will receive one of three possible outputs:
  - `true true` - The URL is definitely in the blacklist (found in both Bloom filter and actual blacklist)
  - `true false` - A false positive from the Bloom filter (URL appears to be in the Bloom filter but isn't in the actual blacklist)
  - `false` - The URL is definitely not in the blacklist

## Stopping the Program
To stop the program, press `Ctrl+C`.
The program will save all blacklisted URLs to the data file. When you restart the container, it will load the previously saved blacklist from the persistent volume.

## Data Persistence
All blacklisted URLs are stored in a file at `data/urlblacklist.txt`. The Docker volume ensures this data persists between container restarts.

## Development
Project Structure

- `src/`: Source code files
- `tests/`: Test files
- `CMakeLists.txt`: CMake configuration
- `Dockerfile`: Docker build configuration

## Testing
Run the tests with:
```
docker run --rm bloomfilter-app ./runTests
```

## Answers to the questions in the task:
- Did we need to modify closed code to add a new command?
No. We used the `ICommand` pattern, so to add a new command, we only had to create a new class that implements the `ICommand` interface and provides an `execute` method with the same signature.
We also added a delete function to the `BloomFilter` class, which did not affect the rest of the class. Additionally, we added an option for the `DELETE` command (resulting in three options for `execute`).

- Did we need to modify closed code to change the names of the commands?
No. We only changed the class names (e.g., from `Add` to `Post` and from `Check` to `Get`). In the `ConfigParser` class, instead of checking command numbers, we extracted the first word of the input as the command. This change did not affect the rest of the code, and the modified part was necessary.

- Did we need to modify closed code to change the output of the commands?
No. We only needed to change the content of the output. For example, instead of printing `False` with `cout << "False"`, we changed it to `cout << "200 OK\n\n" << "False"`.
Since the output was handled inside the command classes, we didn’t need to modify any other parts of the code — just the `execute` method in each command class.

- Did we need to modify closed code to change the I/O sources?
No. Instead of writing output directly to `cout` inside the `execute` functions, we changed those functions to return a `string`. Then, the server handled the actual output.
As for the input, instead of reading from the terminal, we received input from the client via the server, and parsing the input string remained the same.