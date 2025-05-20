#include "ThreadPool.h"
#include <iostream>
#include <thread>
#include <vector>
#include <queue>
#include <mutex>
#include <condition_variable>
#include <atomic>
#include "MainLoop.h"
#include <cstring>
#include <unistd.h>
#include <sys/socket.h>

void ThreadPool::workerFunction() {
    while (true) {
        int clientSocket;

        {
            std::unique_lock<std::mutex> lock(queueMutex);
            condition.wait(lock, [this] { return stop || !tasks.empty(); });

            if (stop && tasks.empty())
                return;

            clientSocket = tasks.front();
            tasks.pop();
        }

        char buffer[4096];
        while (true) {
            memset(buffer, 0, sizeof(buffer));
            int bytes_read = recv(clientSocket, buffer, sizeof(buffer) - 1, 0);
            if (bytes_read <= 0) break;

            std::string request(buffer);
            std::string response = loop.run(request);
            send(clientSocket, response.c_str(), response.length(), 0);
        }

        close(clientSocket);
        std::cout << "Client disconnected\n";
    }
}

ThreadPool::ThreadPool(size_t numThreads, MainLoop& loopRef) : stop(false), loop(loopRef) {
    for (size_t i = 0; i < numThreads; ++i) {
        workers.emplace_back([this]() { workerFunction(); });
    }
}

ThreadPool::~ThreadPool() {
    {
        std::unique_lock<std::mutex> lock(queueMutex);
        stop = true;
    }
    condition.notify_all();
    for (std::thread &worker : workers) {
        if (worker.joinable())
            worker.join();
    }
}

void ThreadPool::addTask(int clientSocket) {
    {
        std::unique_lock<std::mutex> lock(queueMutex);
        tasks.push(clientSocket);
    }
    condition.notify_one();
}
