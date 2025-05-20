#include <gtest/gtest.h>
#include <thread>
#include <vector>
#include <atomic>
#include <chrono>
#include "ThreadPool.h"
#include "MainLoop.h"

// عدל הגלובלי לספירת המשימות הפועלות כרגע
static std::atomic<int> activeTasks{0};

// DummyMainLoop שיורש מ-MainLoop כדי לעקוב אחרי קריאות run()
class DummyMainLoop : public MainLoop {
public:
    // MainLoop דורש string& כפרמטר, לכן נקבל reference ל־config בשורת הבנייה
    DummyMainLoop(std::string& configLine)
      : MainLoop(configLine)
    {}

    // override של run כדי לספור מתי נכנסים ויוצאים
    std::string run(std::string input) override {
        activeTasks.fetch_add(1, std::memory_order_relaxed);
        // סימולציה של עבודה
        std::this_thread::sleep_for(std::chrono::milliseconds(5));
        activeTasks.fetch_sub(1, std::memory_order_relaxed);
        return "OK";
    }
};

TEST(ThreadPoolTest, ConcurrencyStress) {
    // הגדרת קונפיג לצורך MainLoop
    std::string config = "100 1";  
    DummyMainLoop loop(config);
    ThreadPool pool(4, loop);      // 4 threads ב־pool

    const int numTasks = 200;      // כמות משימות בסטרס
    for (int i = 0; i < numTasks; ++i) {
        pool.addTask(i);           // i משמש כ-“socket fd” מזויף
    }

    // מחכים מספיק זמן כדי שכל המשימות יסיימו
    std::this_thread::sleep_for(std::chrono::seconds(2));

    // בסוף לא אמורה להישאר אף משימה פעילה
    EXPECT_EQ(activeTasks.load(), 0);
}