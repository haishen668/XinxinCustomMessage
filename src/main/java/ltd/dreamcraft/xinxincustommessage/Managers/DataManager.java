package ltd.dreamcraft.xinxincustommessage.Managers;

import ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: haishen668
 * @CreateTime: 2024-05-07
 * @Description: 数据管理器
 * @Version: 1.0
 */
public class DataManager {
    public static Map<String, AtomicInteger> invokeCountsMap = new ConcurrentHashMap<>();

    public DataManager() {
        register();
    }

    public static void increment(String key) {
        invokeCountsMap.computeIfAbsent(key, k -> new AtomicInteger(0)).incrementAndGet();
    }

    public static int getCount(String key) {
        AtomicInteger val = invokeCountsMap.get(key);
        return val != null ? val.get() : 0;
    }

    public static void saveCounts() {
        File file = new File(XinxinCustomMessage.getInstance().getDataFolder(), "counts.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("images", getCount("images"));
        config.set("total", getCount("total"));
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void register() {
        File file = new File(XinxinCustomMessage.getInstance().getDataFolder(), "counts.yml");
        if (!file.exists()) {
            XinxinCustomMessage.getInstance().saveResource("counts.yml", true);
        }
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        int images = configuration.getInt("images");
        int total = configuration.getInt("total");

        // 使用 put 替换而非 clear + put，避免中间状态
        invokeCountsMap.put("images", new AtomicInteger(images));
        invokeCountsMap.put("total", new AtomicInteger(total));
    }
}
