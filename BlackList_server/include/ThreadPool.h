#ifndef THREADPOOL_H
#define THREADPOOL_H
#include <iostream>
#include <thread>
#include <vector>
#include <queue>
#include <mutex>
#include <condition_variable>
#include <atomic>
#include "MainLoop.h"


class ThreadPool {
private:
    std::vector<std::thread> workers;
    std::queue<int> tasks;
    std::mutex queueMutex;
    std::condition_variable condition;
    std::atomic<bool> stop;
    MainLoop& loop;

    void workerFunction();

public:
    ThreadPool(size_t numThreads, MainLoop& loop);
    ~ThreadPool();
    void addTask(int clientSocket);
};
#endif // THREADPOOL_H
