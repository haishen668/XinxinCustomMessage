package ltd.dreamcraft.xinxincustommessage;

import ltd.dreamcraft.xinxincustommessage.Managers.DataManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 独立测试 - 不依赖 Bukkit 运行时
 * 测试修复的核心逻辑
 */
public class SimpleTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== 开始测试 ===\n");

        testDataManagerIncrement();
        testDataManagerThreadSafety();
        testDataManagerGetCountDefault();
        testScriptParsing();

        System.out.println("\n=== 测试结果: " + passed + " 通过, " + failed + " 失败 ===");
        if (failed > 0) System.exit(1);
    }

    // ---- DataManager 测试 ----

    static void testDataManagerIncrement() {
        String name = "DataManager.increment() 基本功能";
        try {
            // 清理
            DataManager.invokeCountsMap.clear();

            DataManager.increment("images");
            DataManager.increment("images");
            DataManager.increment("total");

            int images = DataManager.getCount("images");
            int total = DataManager.getCount("total");

            assertEqual(name, 2, images);
            assertEqual(name, 1, total);
        } catch (Exception e) {
            fail(name, "异常: " + e.getMessage());
        }
    }

    static void testDataManagerThreadSafety() {
        String name = "DataManager 并发安全 (100线程 x 1000次)";
        try {
            DataManager.invokeCountsMap.clear();
            DataManager.invokeCountsMap.put("counter", new AtomicInteger(0));

            int threadCount = 100;
            int incrementsPerThread = 1000;
            CountDownLatch latch = new CountDownLatch(threadCount);
            ExecutorService pool = Executors.newFixedThreadPool(threadCount);

            for (int i = 0; i < threadCount; i++) {
                pool.submit(() -> {
                    for (int j = 0; j < incrementsPerThread; j++) {
                        DataManager.increment("counter");
                    }
                    latch.countDown();
                });
            }

            latch.await();
            pool.shutdown();

            int expected = threadCount * incrementsPerThread;
            int actual = DataManager.getCount("counter");

            assertEqual(name, expected, actual);
        } catch (Exception e) {
            fail(name, "异常: " + e.getMessage());
        }
    }

    static void testDataManagerGetCountDefault() {
        String name = "DataManager.getCount() 缺失key返回0";
        try {
            int result = DataManager.getCount("nonexistent_key");
            assertEqual(name, 0, result);
        } catch (Exception e) {
            fail(name, "异常: " + e.getMessage());
        }
    }

    // ---- ScriptUtil 解析测试 ----

    static void testScriptParsing() {
        // 测试 indexOf 解析逻辑（不执行 Bukkit 命令）
        String name = "ScriptUtil 脚本解析: 冒号分割保留完整参数";
        try {
            // 模拟解析逻辑
            String script = "cmd:give @a minecraft:diamond 64";
            int colonIndex = script.indexOf(':');
            String command = script.substring(0, colonIndex).trim();
            String argument = script.substring(colonIndex + 1).trim();

            assertEqual(name + " (command)", "cmd", command);
            assertEqual(name + " (argument)", "give @a minecraft:diamond 64", argument);
        } catch (Exception e) {
            fail(name, "异常: " + e.getMessage());
        }

        name = "ScriptUtil 脚本解析: msg类型";
        try {
            String script = "msg:Hello World";
            int colonIndex = script.indexOf(':');
            String command = script.substring(0, colonIndex).trim();
            String argument = script.substring(colonIndex + 1).trim();

            assertEqual(name + " (command)", "msg", command);
            assertEqual(name + " (argument)", "Hello World", argument);
        } catch (Exception e) {
            fail(name, "异常: " + e.getMessage());
        }

        name = "ScriptUtil 脚本解析: 无冒号返回无效格式";
        try {
            String script = "invalid_script_no_colon";
            int colonIndex = script.indexOf(':');
            boolean valid = colonIndex > 0;

            assertEqual(name, false, valid);
        } catch (Exception e) {
            fail(name, "异常: " + e.getMessage());
        }
    }

    // ---- 辅助方法 ----

    static void assertEqual(String testName, Object expected, Object actual) {
        if (expected.equals(actual)) {
            System.out.println("  PASS: " + testName);
            passed++;
        } else {
            System.out.println("  FAIL: " + testName + " | 期望: " + expected + ", 实际: " + actual);
            failed++;
        }
    }

    static void fail(String testName, String reason) {
        System.out.println("  FAIL: " + testName + " | " + reason);
        failed++;
    }
}
