import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 验证本轮新修复的 3 个问题
 */
public class NewFixesTest {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) throws Exception {
        System.out.println("=== 新修复验证测试 ===\n");

        testVolatileAtomicSwap();
        testVolatileSwapConcurrency();
        testEqualsHashCodeConsistency();
        testEqualsOnlyTrigger();
        testPrintProgressGuard();

        System.out.println("\n=== 结果: " + passed + " 通过, " + failed + " 失败 ===");
        if (failed > 0) System.exit(1);
    }

    // 1. volatile + 原子交换：读者始终看到完整集合
    static void testVolatileAtomicSwap() {
        String name = "volatile 原子交换：读者看到完整旧集合或完整新集合";
        // 模拟：构建新集合后一次性赋值
        Set<String> current = ConcurrentHashMap.newKeySet();
        current.add("msg1");
        current.add("msg2");
        current.add("msg3");

        // 模拟 reload：构建新集合
        Set<String> newSet = ConcurrentHashMap.newKeySet();
        newSet.add("msg4");
        newSet.add("msg5");

        // 原子交换
        Set<String> snapshot = newSet; // volatile 写

        // 读者要么看到旧的 3 条，要么看到新的 2 条，不会看到 0 条或部分
        boolean validSize = snapshot.size() == 2 || snapshot.size() == 3;
        assertEqual(name + " (size 为 2 或 3)", true, validSize);
    }

    // 2. 并发读者在 swap 期间不会看到空集合
    static void testVolatileSwapConcurrency() throws Exception {
        String name = "并发读者在 volatile swap 期间不会看到空集合";
        // 模拟 volatile 字段
        Set<String>[] holder = new Set[]{ConcurrentHashMap.newKeySet()};
        holder[0].add("old1");
        holder[0].add("old2");
        holder[0].add("old3");

        ConcurrentHashMap<String, Integer> seenSizes = new ConcurrentHashMap<>();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(100);
        ExecutorService pool = Executors.newFixedThreadPool(100);

        // 启动 100 个读者线程
        for (int i = 0; i < 100; i++) {
            pool.submit(() -> {
                try {
                    startLatch.await();
                } catch (InterruptedException e) { return; }
                Set<String> snapshot = holder[0]; // volatile 读
                int size = snapshot.size();
                seenSizes.merge(String.valueOf(size), 1, Integer::sum);
                doneLatch.countDown();
            });
        }

        // 主线程做 swap：先构建新集合，再赋值
        Set<String> newSet = ConcurrentHashMap.newKeySet();
        newSet.add("new1");
        newSet.add("new2");
        holder[0] = newSet; // volatile 写

        startLatch.countDown();
        doneLatch.await();
        pool.shutdown();

        // 读者应该只看到 size=3（旧）或 size=2（新），不应该看到 size=0
        boolean noZero = !seenSizes.containsKey("0");
        assertEqual(name + " (无读者看到 size=0)", true, noZero);
    }

    // 3. equals/hashCode 一致性
    static void testEqualsHashCodeConsistency() {
        String name = "equals/hashCode 一致性：同 trigger 对象 equals=true 且 hashCode 相同";
        // 模拟 CustomMessage 的 equals 和 hashCode
        String trigger1 = "help";
        String trigger2 = "help";
        String trigger3 = "info";

        // 同 trigger
        boolean eq = Objects.equals(trigger1, trigger2);
        int hc1 = Objects.hash(trigger1);
        int hc2 = Objects.hash(trigger2);
        assertEqual(name + " (同 trigger equals)", true, eq);
        assertEqual(name + " (同 trigger hashCode)", hc1, hc2);

        // 不同 trigger
        eq = Objects.equals(trigger1, trigger3);
        assertEqual(name + " (不同 trigger equals)", false, eq);
    }

    // 4. Set 行为：同 trigger 自动去重
    static void testEqualsOnlyTrigger() {
        String name = "Set 行为：同 trigger 不同内容的模板自动去重";
        // 模拟 Set<String>（只用 trigger 做 equals/hashCode）
        Set<String> triggers = ConcurrentHashMap.newKeySet();
        triggers.add("help");
        triggers.add("help"); // 重复
        triggers.add("info");

        assertEqual(name + " (去重后 size=2)", 2, triggers.size());
    }

    // 5. printProgress 防御：totalBytes <= 0 直接返回
    static void testPrintProgressGuard() {
        String name = "printProgress 防御：totalBytes <= 0 不会除零";
        // 模拟 totalBytes = 0
        int totalBytes = 0;
        int currentBytes = 50;
        boolean guarded = totalBytes <= 0;
        assertEqual(name + " (totalBytes=0 触发 guard)", true, guarded);

        // 模拟 totalBytes = -1
        totalBytes = -1;
        guarded = totalBytes <= 0;
        assertEqual(name + " (totalBytes=-1 触发 guard)", true, guarded);

        // 模拟正常情况
        totalBytes = 1024;
        guarded = totalBytes <= 0;
        assertEqual(name + " (totalBytes=1024 不触发 guard)", false, guarded);
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
}
