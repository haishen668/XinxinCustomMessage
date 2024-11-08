package ltd.dreamcraft.xinxincustommessage;


import ltd.dreamcraft.xinxincustommessage.CustomMessageHook.CustomHook;
import ltd.dreamcraft.xinxincustommessage.Managers.DataManager;
import ltd.dreamcraft.xinxincustommessage.api.CustomMessageAPI;
import ltd.dreamcraft.xinxincustommessage.listeners.MessageListener;
import ltd.dreamcraft.xinxincustommessage.objects.CustomImage;
import ltd.dreamcraft.xinxincustommessage.objects.CustomMessage;
import ltd.dreamcraft.xinxincustommessage.objects.CustomText;
import ltd.dreamcraft.xinxincustommessage.objects.SubImage;
import ltd.dreamcraft.xinxincustommessage.utils.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class XinxinCustomMessage extends JavaPlugin {
    public static XinxinCustomMessage instance;

    public static Set<CustomMessage> customMessageList = ConcurrentHashMap.newKeySet();

    public static Set<CustomImage> customImageList = ConcurrentHashMap.newKeySet();
    public static Map<String, Font> customFontList = new HashMap<>();
    public static boolean LOG = false;
    private static ScriptEngine scriptEngine;
    private static CustomHook customHook;
    public static XinxinCustomMessage getInstance() {
        return instance;
    }

    public static void loadAllFonts() {
        customFontList.clear();
        File directory = new File(XinxinCustomMessage.getInstance().getDataFolder(), "fonts");
        if (!directory.exists()) {
            directory.mkdir();
        }
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".ttf")) {
                    try {
                        Font font = Font.createFont(Font.TRUETYPE_FONT, file);
                        customFontList.put(file.getName().replaceAll("\\.t(t|T)f", "").trim(), font);
                    } catch (FontFormatException | IOException e) {
                        XinxinCustomMessage.getInstance().getLogger().severe(file.getName() + "可能不是一个font文件");
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public static List<SubImage> loadSubImage(Configuration config, String key) {
        List<SubImage> subImageList = new ArrayList<>();
        ConfigurationSection subImages = config.getConfigurationSection("custom_images." + key + ".images");
        if (subImages != null)
            for (String subImageID : subImages.getKeys(false)) {
                String path = subImages.getString(subImageID + ".path");
                int w = subImages.getInt(subImageID + ".width");
                int h = subImages.getInt(subImageID + ".height");
                int x = subImages.getInt(subImageID + ".x");
                int z = subImages.getInt(subImageID + ".z");
                boolean center = subImages.getBoolean(subImageID + ".center", false);
                SubImage subImage = new SubImage(subImageID, path, w, h, x, z, center);
                subImageList.add(subImage);
            }
        return subImageList;
    }

    public static List<CustomText> loadCustomTexts(Configuration config, String key) {
        List<CustomText> customTextList = new ArrayList<>();
        ConfigurationSection customTexts = config.getConfigurationSection("custom_images." + key + ".texts");
        if (customTexts != null)
            for (String textID : customTexts.getKeys(false)) {
                String text = customTexts.getString(textID + ".text");
                int x = customTexts.getInt(textID + ".x");
                int z = customTexts.getInt(textID + ".z");
                String fontName = customTexts.getString(textID + ".font");
                int style = customTexts.getInt(textID + ".style");
                int size = customTexts.getInt(textID + ".size");
                //Font font = new Font(fontName, style, size);
                Font font = customFontList.get(fontName);
                if (font == null) {
                    //如果字体不在加载列表中就使用默认字体
                    font = new Font(fontName, style, size);
                } else {
                    //如果在就设置style和size
                    font = font.deriveFont(style, size);
                }
//                String colorString = customTexts.getString(textID + ".color");
//                ArrayList<Integer> colors = new ArrayList<>();
//                for (String s : colorString.split("\\|"))
//                    colors.add(Integer.valueOf(Integer.parseInt(s)));
//                Color color = new Color(((Integer) colors.get(0)).intValue(), ((Integer) colors.get(1)).intValue(), ((Integer) colors.get(2)).intValue());
                boolean center = customTexts.getBoolean(textID + ".center", false);
                CustomText customText = new CustomText(textID, text, font, x, z, center);
                customTextList.add(customText);
            }
        return customTextList;
    }

    public static void loadCustomMessage(Configuration config) {
        ConfigurationSection messages = config.getConfigurationSection("custom_messages");
        if (messages != null) {
            for (String key : messages.getKeys(false)) {
                String trigger = messages.getString(key + ".trigger");
                List<String> responses = messages.contains(key + ".responses") ? messages.getStringList(key + ".responses") : Collections.emptyList();
                List<String> unbindMessages = messages.contains(key + ".unbind_messages") ? messages.getStringList(key + ".unbind_messages") : Collections.emptyList();
                List<Long> groups = messages.contains(key + ".groups") ? messages.getLongList(key + ".groups") : Collections.emptyList();
                List<Long> admins = messages.contains(key + ".admins") ? messages.getLongList(key + ".admins") : Collections.emptyList();
                List<String> scripts = messages.contains(key + ".scripts") ? messages.getStringList(key + ".scripts") : Collections.emptyList();
                CustomMessage customMessage = new CustomMessage(trigger, responses, unbindMessages, groups, key, admins, scripts);
                customMessageList.add(customMessage);
            }
        }
        ConfigurationSection customImages = config.getConfigurationSection("custom_images");
        if (customImages != null) {
            for (String key : customImages.getKeys(false)) {
                String source = config.getString("custom_images." + key + ".source");
                int width = config.getInt("custom_images." + key + ".width");
                int height = config.getInt("custom_images." + key + ".height");
                CustomImage customImage = new CustomImage(key, source, width, height, loadSubImage(config, key), loadCustomTexts(config, key));
                customImageList.add(customImage);
            }
        }
    }

    public static void loadCustomMessages() {
        customMessageList.clear();
        customImageList.clear();
        loadCustomMessage(getInstance().getConfig());
        List<String> folders = getInstance().getConfig().getStringList("messagefolders");
        for (String name : folders) {
            File folder = new File(instance.getDataFolder(), name);
            if (folder.exists() && !folder.isDirectory())
                continue;
            if (!folder.exists())
                folder.mkdirs();
            if (folder.isDirectory())
                for (File file : folder.listFiles()) {
                    if (file.getName().endsWith(".yml"))
                        try {
                            loadCustomMessage((Configuration) YamlConfiguration.loadConfiguration(file));
                        } catch (Exception e) {
                            if (getInstance().getConfig().getBoolean("debug")) {
                                e.printStackTrace();
                            } else {
                                getInstance().getLogger().warning("无法载入消息文件: " + file.getName() + "打开debug查看详细信息");
                            }
                        }
                }
        }
        instance.getLogger().info("§a载入了" + customMessageList.size() + "条自定义信息以及" + customImageList.size() + "条自定义图片" + customFontList.size() + "个自定义字体");
    }

    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        //hook注册
        customHook = new CustomHook();
        customHook.register();
        File file = new File(getDataFolder(), "images/个人信息.png");
        if (!file.exists()) {
            saveResource("images/个人信息.png", false);
        }
        file = new File(getDataFolder(), "images/在线人数.png");
        if (!file.exists()) {
            saveResource("images/在线人数.png", false);
        }
        file = new File(getDataFolder(), "images/精灵背包.png");
        if (!file.exists()) {
            saveResource("images/精灵背包.png", false);
        }

        if (Bukkit.getPluginManager().getPlugin("PokemonBag") != null) {
            File pokeDir = new File(getDataFolder(), "images/pokemonImg");
            if (!pokeDir.exists()) {
                pokeDir.mkdir();
                boolean isDownload = saveWebResource("https://pan.dreamcraft.ltd/directlink/local/plugins/PokemonBag/resource/pokemonImg.zip", "images/pokemonImg.zip", false);
                try {
                    if (isDownload) {
                        File unzipDir = new File(getDataFolder(), "images");
                        unzip(unzipDir.getPath() + "/pokemonImg.zip", true);
                    }
                } catch (IOException e) {
                    Bukkit.getConsoleSender().sendMessage("§f[§e" + this.getName() + "§f]§7 " + "pokemonImg.zip" + " §a解压失败");
                }
            }
            File pokeBagFile = new File(getDataFolder(), "messages/pokemonbag.yml");
            if (!pokeBagFile.exists()) {
                saveWebResource("https://pan.dreamcraft.ltd/directlink/local/plugins/PokemonBag/resource/pokemonbag.yml", "messages/pokemonbag.yml", false);
            }
        }

        new DataManager();
        getServer().getPluginManager().registerEvents(new MessageListener(), this);
        loadAllFonts();
        loadCustomMessages();
        getLogger().info("Loaded");
        Metrics metrics = new Metrics(this, 21808);
        // 初始化JS引擎
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        scriptEngine = scriptEngineManager.getEngineByName("JavaScript");

    }

    public void unzip(String zipFilePath, boolean delete) throws IOException {
        // 创建解压后文件的目标文件夹
        File destDir = new File(zipFilePath.substring(0, zipFilePath.lastIndexOf("/")));
        if (!destDir.exists()) {
            destDir.mkdirs(); // 如果目标文件夹不存在，则创建
        }

        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            // 获取ZIP文件中的所有条目
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                // 生成解压后的目标文件路径
                File entryDestination = new File(destDir, entry.getName());

                // 如果是文件夹，创建文件夹
                if (entry.isDirectory()) {
                    if (!entryDestination.exists()) {
                        entryDestination.mkdirs(); // 创建文件夹
                    }
                } else {
                    // 如果是文件，先确保父目录存在
                    File parentDir = entryDestination.getParentFile();
                    if (parentDir != null && !parentDir.exists()) {
                        parentDir.mkdirs(); // 创建父目录
                    }

                    // 解压文件
                    try (InputStream in = zipFile.getInputStream(entry);
                         OutputStream out = Files.newOutputStream(entryDestination.toPath())) {

                        byte[] buffer = new byte[1024]; // 缓冲区
                        int len;
                        while ((len = in.read(buffer)) > 0) {
                            out.write(buffer, 0, len); // 写入文件
                        }
                    }
                }
            }
        } catch (IOException e) {
            // 捕获并处理解压异常
            throw new IOException("解压ZIP文件时发生错误: " + zipFilePath, e);
        }

        // 如果需要删除ZIP文件，则删除
        if (delete) {
            File zipFileToDelete = new File(zipFilePath);
            if (zipFileToDelete.exists() && zipFileToDelete.delete()) {
                // 输出日志，表示解压成功并已删除ZIP文件
                Bukkit.getConsoleSender().sendMessage("§f[§e" + this.getName() + "§f] §7§a资源解压成功");
            } else {
                // 如果删除失败，输出失败日志
                Bukkit.getConsoleSender().sendMessage("§cZIP文件解压成功，但删除文件失败: " + zipFilePath);
            }
        } else {
            // 输出日志，表示解压成功但未删除ZIP文件
            Bukkit.getConsoleSender().sendMessage("§aZIP文件解压成功: " + zipFilePath);
        }
    }

    public boolean saveWebResource(String url, String resourcePath, boolean replace) {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL不能为空或为空字符串");
        }

        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("路径不能为空");
        }

        File outFile = new File("plugins/" + this.getName(), resourcePath);
        File outDir = outFile.getParentFile();

        String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        if (!outFile.exists() || replace) {
            try {
                URL website = new URL(url);
                URLConnection connection = website.openConnection();
                int contentLength = connection.getContentLength();

                if (contentLength == -1) {
                    getLogger().log(Level.WARNING, "无法确定文件大小");
                }

                try (InputStream in = connection.getInputStream();
                     OutputStream out = Files.newOutputStream(outFile.toPath())) {

                    printProgress(fileName, 0, contentLength);

                    byte[] buf = new byte[1024];
                    int bytesRead;
                    int totalBytesRead = 0;
                    while ((bytesRead = in.read(buf)) != -1) {
                        out.write(buf, 0, bytesRead);
                        totalBytesRead += bytesRead;
                        printProgress(fileName, totalBytesRead, contentLength);
                    }
                    Bukkit.getConsoleSender().sendMessage("§f[§e" + this.getName() + "§f] §7" + fileName + " §a下载成功");
                    return true; // 下载成功
                }
            } catch (IOException ex) {
                Bukkit.getConsoleSender().sendMessage("§f[§e" + this.getName() + "§f]§7 " + fileName + " §c下载失败");
                return false; // 下载失败
            }
        } else {
            getLogger().log(Level.WARNING, "无法保存资源，因为文件 " + outFile.getName() + " 已存在");
            return false; // 文件已存在，不进行下载
        }
    }

    // 辅助类，用于存储每个文件的进度信息
    private static class ProgressInfo {
        List<Integer> steps; // 需要输出进度的百分比步骤
        int currentStepIndex; // 当前需要输出的步骤索引
        String[] symbols = {"|", "/", "-", "\\"}; // 进度条旋转符号
        int symbolIndex; // 当前符号的索引

        ProgressInfo(List<Integer> steps) {
            this.steps = steps;
            this.currentStepIndex = 0;
            this.symbolIndex = 0;
        }

        // 获取下一个符号
        String getNextSymbol() {
            String symbol = symbols[symbolIndex];
            symbolIndex = (symbolIndex + 1) % symbols.length;
            return symbol;
        }
    }

    // 存储每个文件的进度信息
    private ConcurrentMap<String, ProgressInfo> progressMap = new ConcurrentHashMap<>();

    /**
     * 打印进度条的方法
     *
     * @param fileName     文件名
     * @param currentBytes 当前已完成的字节数
     * @param totalBytes   总字节数
     */
    public void printProgress(String fileName, int currentBytes, int totalBytes) {
        // 计算当前百分比
        int percentage = (int) ((currentBytes / (double) totalBytes) * 100);

        // 获取或初始化 ProgressInfo
        ProgressInfo progressInfo = progressMap.computeIfAbsent(fileName, key -> {
            // 随机选择2-5个步骤
            int stepCount = ThreadLocalRandom.current().nextInt(2, 6);
            Set<Integer> stepSet = new TreeSet<>();
            while (stepSet.size() < stepCount) {
                // 随机生成1-99的百分比，避免0和100
                int step = ThreadLocalRandom.current().nextInt(1, 100);
                stepSet.add(step);
            }
            List<Integer> steps = new ArrayList<>(stepSet);
            steps.add(100); // 确保100%总是输出
            return new ProgressInfo(steps);
        });

        // 检查是否达到了当前步骤
        while (progressInfo.currentStepIndex < progressInfo.steps.size() && percentage >= progressInfo.steps.get(progressInfo.currentStepIndex)) {
            int currentStepPercentage = progressInfo.steps.get(progressInfo.currentStepIndex);
            String symbol = progressInfo.getNextSymbol();

            // 生成进度条
            StringBuilder progressBar = new StringBuilder("[");
            int progressMarks = currentStepPercentage / 5; // 每5%一个"="
            for (int i = 0; i < 20; i++) { // 总长度20
                if (i < progressMarks) {
                    progressBar.append("=");
                } else {
                    progressBar.append(" ");
                }
            }
            progressBar.append("] ");

            // 输出进度
            getLogger().info(String.format("%s %s %d%%", fileName, progressBar.toString(), currentStepPercentage));

            progressInfo.currentStepIndex++;
        }

        // 如果达到或超过100%，移除进度信息
        if (percentage >= 100) {
            progressMap.remove(fileName);
        }
    }

    public void onDisable() {
        DataManager.saveCounts();
        customHook.unregister();
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("xinxincustommessages.admin")) {
            if (args[0].equalsIgnoreCase("reload")) {
                DataManager.saveCounts();
                customFontList.clear();
                reloadConfig();
                loadAllFonts();
                loadCustomMessages();
                //加载 DataManager.invokeCountsMap 数据
                new DataManager();
                sender.sendMessage("§a已经重新载入配置文件");
                return true;
            }
            if (args[0].equalsIgnoreCase("fonts")) {
                sender.sendMessage("§a默认字体列表: ");
                for (Font font : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts())
                    sender.sendMessage(font.getName());
                sender.sendMessage("§a自定义拓展字体列表: ");
                Set<String> extraFont = customFontList.keySet();
                Iterator<String> iterator = extraFont.iterator();
                while (iterator.hasNext()) {
                    sender.sendMessage(iterator.next());
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("log")) {
                LOG = !LOG;
                sender.sendMessage("§a转换消息状态已切换为: " + LOG);
                return true;
            }
            if (args[0].equalsIgnoreCase("listmessages")) {
                sender.sendMessage("§a已经加载的消息模版数量: " + customMessageList.size());
                for (CustomMessage customMessage : customMessageList)
                    sender.sendMessage("§b" + customMessage.getId());
                sender.sendMessage("§a======输出完毕======");
                return true;
            }
            if (args[0].equalsIgnoreCase("listimages")) {
                sender.sendMessage("§a已经加载的自定义图片模版数量: " + customImageList.size());
                for (CustomImage customImage : customImageList)
                    sender.sendMessage("§b" + customImage.id);
                sender.sendMessage("§a======输出完毕======");
                return true;
            }
        }
        if (args.length == 4 && sender.hasPermission("xinxincustommessages.send") && args[0].equalsIgnoreCase("send")) {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                if (CustomMessageAPI.sendCustomMessage(Long.parseLong(args[1]), args[2], args[3], "")) {
                    sender.sendMessage("§a发送成功!");
                } else {
                    sender.sendMessage("§c消息id不存在或者群号不是数字");
                }
            });
            return true;
        }
        if (args.length == 5 && sender.hasPermission("xinxincustommessages.send") && args[0].equalsIgnoreCase("send")) {
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                if (CustomMessageAPI.sendCustomMessage(Long.parseLong(args[1]), args[2], args[3], args[4])) {
                    sender.sendMessage("§a发送成功!");
                } else {
                    sender.sendMessage("§c消息id不存在或者群号不是数字");
                }
            });
            return true;
        }
        // 添加插件名称和版本信息
        sender.sendMessage("  §f§lXinxinCustomMessage[Help] " + XinxinCustomMessage.getInstance().getDescription().getVersion());
        sender.sendMessage("");
        sender.sendMessage("  §7[命令]: §f/xxcm §7[...]");

        // 列出各个命令及其描述
        sender.sendMessage("§7     - §freload");
        sender.sendMessage("§7       重载配置文件");
        sender.sendMessage("§7     - §ffonts");
        sender.sendMessage("§7       查看字体列表");
        sender.sendMessage("§7     - §flog");
        sender.sendMessage("§7       开启或关闭输出消息的mirai码");
        sender.sendMessage("§7     - §flistmessages");
        sender.sendMessage("§7       列出已经加载的自定义消息模版");
        sender.sendMessage("§7     - §flistimages");
        sender.sendMessage("§7       列出已经加载的自定义图片模版");
        sender.sendMessage("§7     - §fsend <群号> <玩家> <消息ID> [可选extra]");
        sender.sendMessage("§7       向群内发送信息");
        return true;
    }

    public static ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    public static void setScriptEngine(ScriptEngine scriptEngine) {
        XinxinCustomMessage.scriptEngine = scriptEngine;
    }
}


