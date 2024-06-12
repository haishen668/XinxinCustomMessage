package ltd.dreamcraft.xinxincustommessage.Managers;

import ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: haishen668
 * @CreateTime: 2024-05-07
 * @Description: 数据管理器
 * @Version: 1.0
 */
public class DataManager {
    public static Map<String, Integer> invokeCountsMap = new HashMap<>();

    public DataManager() {
        invokeCountsMap.clear();
        register();
    }

    public static void saveCounts() {
        File file = new File(XinxinCustomMessage.getInstance().getDataFolder(), "counts.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.set("images", invokeCountsMap.get("images"));
        config.set("total", invokeCountsMap.get("total"));
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //初始化
    private void register() {
        //创建命令数量统计文本
        File file = new File(XinxinCustomMessage.getInstance().getDataFolder(), "counts.yml");
        if (!file.exists()) {//判断文件是否存在，不存在则创建
            XinxinCustomMessage.getInstance().saveResource("counts.yml", true);
        }
        FileConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        int images = configuration.getInt("images");
        int total = configuration.getInt("total");

        invokeCountsMap.put("images", images);
        invokeCountsMap.put("total", total);

    }
}
