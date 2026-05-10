import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 独立测试 - 验证修复中使用的核心模式
 * 无需项目依赖，直接 javac 编译运行
 */
public class StandaloneTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== XinxinCustomMessage 修复验证测试 ===\n");

        testAtomicIncrement();
        testConcurrentHashMapSafety();
        testGetCountDefault();
        testScriptParsing();
        testColorFallback();
        testNullCheckOrder();

        System.out.println("\n=== 结果: " + passed + " 通过, " + failed + " 失败 ===");
        if (failed > 0) System.exit(1);
    }

    // 1. 验证 AtomicInteger 原子递增
    static void testAtomicIncrement() {
        String name = "AtomicInteger.incrementAndGet 原子递增";
        Map<String, AtomicInteger> map = new ConcurrentHashMap<>();
        map.computeIfAbsent("images", k -> new AtomicInteger(0)).incrementAndGet();
        map.computeIfAbsent("images", k -> new AtomicInteger(0)).incrementAndGet();
        map.computeIfAbsent("images", k -> new AtomicInteger(0)).incrementAndGet();

        AtomicInteger val = map.get("images");
        assertEqual(name, 3, val.get());
    }

    // 2. 验证 ConcurrentHashMap 并发安全
    static void testConcurrentHashMapSafety() throws Exception {
        String name = "ConcurrentHashMap + AtomicInteger 并发安全 (100线程 x 10000次)";
        Map<String, AtomicInteger> map = new ConcurrentHashMap<>();
        map.put("counter", new AtomicInteger(0));

        int threadCount = 100;
        int perThread = 10000;
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                for (int j = 0; j < perThread; j++) {
                    map.computeIfAbsent("counter", k -> new AtomicInteger(0)).incrementAndGet();
                }
                latch.countDown();
            });
        }

        latch.await();
        pool.shutdown();

        assertEqual(name, threadCount * perThread, map.get("counter").get());
    }

    // 3. 验证缺失 key 返回默认值 0
    static void testGetCountDefault() {
        String name = "getCount 缺失 key 返回 0";
        Map<String, AtomicInteger> map = new ConcurrentHashMap<>();
        AtomicInteger val = map.get("missing");
        int result = val != null ? val.get() : 0;
        assertEqual(name, 0, result);
    }

    // 4. 验证 indexOf 解析保留冒号后内容
    static void testScriptParsing() {
        // cmd:give @a minecraft:diamond 64 → 应保留完整 "give @a minecraft:diamond 64"
        String name = "indexOf 解析保留含冒号的参数";
        String script = "cmd:give @a minecraft:diamond 64";
        int colonIndex = script.indexOf(':');
        String command = script.substring(0, colonIndex).trim();
        String argument = script.substring(colonIndex + 1).trim();

        assertEqual(name + " (command)", "cmd", command);
        assertEqual(name + " (argument)", "give @a minecraft:diamond 64", argument);

        // 对比旧的 split(":") 行为
        name = "旧 split(\":\") 会截断参数（对比验证）";
        String[] parts = script.split(":");
        String oldArgument = parts.length > 1 ? parts[1].trim() : "";
        assertEqual(name, "give @a minecraft", oldArgument); // 旧逻辑会丢失 ":diamond 64"
    }

    // 5. 验证无效颜色码返回 Color.BLACK 而非 null
    static void testColorFallback() {
        String name = "无效颜色码返回 Color.BLACK";
        // 模拟 TextUtil.parseColor 对无效码的处理
        String colorCode = "&g"; // 无效的颜色码
        Color result = parseColorSafe(colorCode);
        assertNotNull(name, result);
        assertEqual(name, Color.BLACK, result);

        name = "有效颜色码 &c 返回红色";
        result = parseColorSafe("&c");
        assertNotNull(name, result);
        assertEqual(name, new Color(255, 85, 85), result);

        name = "null 输入返回 Color.BLACK";
        result = parseColorSafe(null);
        assertNotNull(name, result);
        assertEqual(name, Color.BLACK, result);
    }

    // 6. 验证 null 检查顺序
    static void testNullCheckOrder() {
        String name = "null 检查顺序: null.extra 不会 NPE";
        String extra = null;
        boolean hasExtra = extra != null && !extra.isEmpty(); // 新写法
        assertEqual(name, false, hasExtra);

        name = "旧写法 null.isEmpty() 会 NPE";
        boolean oldCrashed = false;
        try {
            boolean oldHasExtra = !extra.isEmpty() && !extra.equals("") && extra != null;
        } catch (NullPointerException e) {
            oldCrashed = true;
        }
        assertEqual(name, true, oldCrashed);
    }

    // 模拟 parseColor 逻辑
    static Color parseColorSafe(String colorCode) {
        if (colorCode != null) {
            if (colorCode.startsWith("&#")) {
                colorCode = colorCode.replace("&#", "");
                try {
                    int intValue = Integer.parseInt(colorCode, 16);
                    return new Color(intValue);
                } catch (NumberFormatException e) {
                    return Color.BLACK;
                }
            } else if (colorCode.startsWith("&")) {
                switch (colorCode) {
                    case "&0": return Color.BLACK;
                    case "&1": return Color.BLUE;
                    case "&2": return Color.GREEN;
                    case "&4": return Color.RED;
                    case "&c": return new Color(255, 85, 85);
                    default: return Color.BLACK; // 修复后的行为
                }
            }
        }
        return Color.BLACK; // 修复后: null 输入返回 BLACK
    }

    // 辅助方法
    static void assertEqual(String name, Object expected, Object actual) {
        if (expected.equals(actual)) {
            System.out.println("  PASS: " + name);
            passed++;
        } else {
            System.out.println("  FAIL: " + name + " | expected: " + expected + ", actual: " + actual);
            failed++;
        }
    }

    static void assertNotNull(String name, Object obj) {
        if (obj != null) {
            System.out.println("  PASS: " + name + " (not null)");
            passed++;
        } else {
            System.out.println("  FAIL: " + name + " | was null");
            failed++;
        }
    }
}
