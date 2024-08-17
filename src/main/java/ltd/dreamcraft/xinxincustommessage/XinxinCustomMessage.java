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
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class XinxinCustomMessage extends JavaPlugin {
    public static XinxinCustomMessage instance;

    public static Set<CustomMessage> customMessageList = ConcurrentHashMap.newKeySet();

    public static Set<CustomImage> customImageList = ConcurrentHashMap.newKeySet();
    public static Map<String, Font> customFontList = new HashMap<>();
    public static boolean LOG = false;
    private static ScriptEngine scriptEngine;
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
        new CustomHook().register();
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
        new DataManager();
        getServer().getPluginManager().registerEvents((Listener) new MessageListener(), (Plugin) this);
        loadAllFonts();
        loadCustomMessages();
        getLogger().info("Loaded");
        Metrics metrics = new Metrics(this, 21808);
        // 初始化JS引擎
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        scriptEngine = scriptEngineManager.getEngineByName("JavaScript");

    }

    public void onDisable() {
        DataManager.saveCounts();
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
//                long id;
//                CustomMessage message = null;
//                for (CustomMessage customMessage : customMessageList) {
//                    if (customMessage.getId().equalsIgnoreCase(args[3])) {
//                        message = customMessage;
//                        break;
//                    }
//                }
//                if (message == null) {
//                    sender.sendMessage("§c消息ID: §b" + args[3] + " §c不存在!");
//                    return;
//                }
//                OfflinePlayer player = Bukkit.getOfflinePlayer(args[2]);
//                try {
//                    id = Long.parseLong(args[1]);
//                } catch (Exception ignored) {
//                    sender.sendMessage("§a群号必须为数字!");
//                    return;
//                }
//                List<String> response = MessageUtil.getMsg(message.getResponses(), player, Math.toIntExact(id), Math.toIntExact(Long.valueOf(0L)), "", "");
//                if (!response.isEmpty())
//                    BotAction.sendGroupMessage((int) id,response, true);
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
        sender.sendMessage("§a/xxcm reload");
        sender.sendMessage("§a/xxcm fonts - 查看字体列表");
        sender.sendMessage("§a/xxcm log - 开启或关闭输出消息的mirai码");
        sender.sendMessage("§a/xxcm listmessages - 列出已经加载的自定义消息模版");
        sender.sendMessage("§a/xxcm listimages - 列出已经加载的自定义图片模版");
        sender.sendMessage("§a/xxcm send <群号> <玩家> <消息ID> [可选extra]- 向群内发送信息");
        return true;
    }

    public static ScriptEngine getScriptEngine() {
        return scriptEngine;
    }

    public static void setScriptEngine(ScriptEngine scriptEngine) {
        XinxinCustomMessage.scriptEngine = scriptEngine;
    }
}


