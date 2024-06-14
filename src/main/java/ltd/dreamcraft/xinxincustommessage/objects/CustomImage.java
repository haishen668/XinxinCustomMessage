package ltd.dreamcraft.xinxincustommessage.objects;

import com.xinxin.BotApi.BotBind;
import ltd.dreamcraft.www.pokemonbag.Utils.ParsePokemon;
import ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage;
import ltd.dreamcraft.xinxincustommessage.utils.TextSplitUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class CustomImage {
    // 图片的唯一标识符
    public final String id;

    // 图片的来源路径
    public final String source;

    // 图片的宽度
    public final int width;

    // 图片的高度
    public final int height;

    // 包含的子图片列表
    public final List<SubImage> subImages;

    // 包含的自定义文本列表
    public final List<CustomText> customTexts;

    public CustomImage(String id, String source, int width, int height, List<SubImage> subImages, List<CustomText> customTexts) {
        this.id = id;
        this.source = source;
        this.width = width;
        this.height = height;
        this.subImages = subImages;
        this.customTexts = customTexts;
    }

    // 下载图片
    public static BufferedImage downloadImage(String link) {
        try {
            URL url = new URL(link);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(20000);
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.addRequestProperty("User-Agent", "Mozilla/5.0");
            connection.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            connection.addRequestProperty("Pragma", "no-cache");
            InputStream in = connection.getInputStream();
            BufferedImage image = ImageIO.read(in);
            in.close();
            return image;
        } catch (Exception e) {
            if (XinxinCustomMessage.getInstance().getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // 调整图片大小
    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, 16);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, 1);
        Graphics2D g = outputImage.createGraphics();
        outputImage = g.getDeviceConfiguration().createCompatibleImage(targetWidth, targetHeight, 3);
        g = outputImage.createGraphics();
        g.drawImage(resultingImage, 0, 0, null);
        return outputImage;
    }

    // 渲染图片
    public BufferedImage renderImage(OfflinePlayer player) throws Exception {
        BufferedImage srcImg;

        // 解析图片路径，包括处理PlaceholderAPI和BotBind
        String path;
        if (player == null) {
            path = this.source;
        } else {
            path = PlaceholderAPI.setPlaceholders(player, this.source);
        }
        if (player != null) {
            path = path.replace("{qq}", BotBind.getBindQQ(player.getName()).toLowerCase());
        }

        // 根据路径类型加载图片
        if (path.startsWith("[url]")) {
            // 如果以[url]开头，下载链接地址的图片
            srcImg = downloadImage(path.substring(5));
        } else {
            // 否则读取本地图片
            File imageFolder = new File(XinxinCustomMessage.getInstance().getDataFolder(), "images");
            File imageFile = new File(imageFolder, path);
            srcImg = ImageIO.read(imageFile);
        }

        // 调整图片大小 如果给定了宽高就重新设置宽高的大小
        if (Math.min(this.width, this.height) > 0) {
            srcImg = resizeImage(srcImg, this.width, this.height);
        }

        Graphics2D g2d = srcImg.createGraphics();
        // 渲染图片底图
        g2d.drawImage(srcImg, 0, 0, srcImg.getWidth(), srcImg.getHeight(), null);

        for (SubImage subImage : this.subImages) {
            BufferedImage image = null;
            String subImgPath = subImage.path;
            subImgPath = PlaceholderAPI.setPlaceholders(player, subImgPath);

            // 处理子图片路径
            if (subImgPath.startsWith("[url]")) {
                if (player != null) {
                    subImgPath = subImgPath.replace("{qq}", BotBind.getBindQQ(player.getName()).toLowerCase());
                }
                image = downloadImage(subImgPath.substring(5));
            } else if (subImgPath.startsWith("[pokemon]")) {
                // 在pokemonbag插件中写一个方法，返回一个image对象
                String imageFolder = "plugins/XinxinCustomMessage/images/pokemonImg";
                if (player != null) {
                    image = ParsePokemon.pokemonToImg(imageFolder, subImgPath, player);
                }
                if (image == null) {
                    continue;
                }
            } else {
                File imageFolder = new File(XinxinCustomMessage.getInstance().getDataFolder(), "images");
                File imageFile = new File(imageFolder, subImgPath);
                image = ImageIO.read(imageFile);
            }

            int w = subImage.width;
            int h = subImage.height;
            int x = subImage.x;
            int z = subImage.z;
            // 如果子图片不为空，调整大小并绘制到主图片上
            if (image != null) {
                if (Math.min(w, h) > 0) {
                    image = resizeImage(image, w, h);
                }
                // 中心绘制
                if (subImage.center) {
//                    x = (srcImg.getWidth() - image.getWidth()) / 2;
                    x -= image.getWidth() / 2;
                    z -= image.getHeight() / 2;
                }
                g2d.drawImage(image, x, z, null);  // 这里的null表示绘制参数为默认值，即无特殊处理
            }
        }

        for (CustomText customText : this.customTexts) {
            String text = customText.text;
            try {
                text = PlaceholderAPI.setPlaceholders(player, text);
                if (player != null) {
                    text = text.replace("{qq}", BotBind.getBindQQ(player.getName()));
                }
            } catch (IllegalStateException e) {
                // 处理异常，打印或记录日志
                if (XinxinCustomMessage.getInstance().getConfig().getBoolean("debug")) {
                    e.printStackTrace();
                } else {
                    XinxinCustomMessage.getInstance().getLogger().warning("字符串: " + text + "中的变量无法正常解析,可能变量无法异步获取,请打开debug查看详细信息");
                }
            }

            // 处理包含Pokemon的文本
            if (text.contains("[pokemon]")) {
                if (player != null) {
                    text = ParsePokemon.pokemonToString(text, player);
                }
                if (text == null) {
                    continue;
                }
            }

            int x = customText.x;
            int z = customText.z;
            Font font = customText.font;
            boolean center = customText.center;

            ArrayList<String> subString = TextSplitUtil.TextSpit(text);  // 切割文本
            ArrayList<String> parseSubText = new ArrayList<>();  // 解析后的文本


            if (subString.isEmpty()) {
                // 如果没有子文本，直接渲染文本
                Color color = TextSplitUtil.ParseColor(TextSplitUtil.GetParseString(text, "color"));  // 解析颜色
                g2d.setFont(font);
                g2d.setColor(color);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // 判断center属性，调整绘制位置
                if (center) {
                    // 计算文本宽度
                    int textWidth = TextSplitUtil.getStringWidth(font, TextSplitUtil.GetParseString(text, "string"));
                    // 计算居中位置
                    x -= textWidth / 2;
                }
                g2d.drawString(TextSplitUtil.GetParseString(text, "string"), x, z);
            } else {
                int totalSubTextWidth = 0;
                for (int i = 0; i < subString.size(); i++) {
                    String subText = subString.get(i);
                    Color color = TextSplitUtil.ParseColor(TextSplitUtil.GetParseString(subText, "color"));
                    if (i - 1 >= 0) {
                        totalSubTextWidth += TextSplitUtil.getStringWidth(font, parseSubText.get(i - 1));
                    }
                    parseSubText.add(TextSplitUtil.GetParseString(subText, "string"));
                    g2d.setFont(font);
                    g2d.setColor(color);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // 判断center属性，调整绘制位置
                    if (center) {
                        // 计算文本宽度
                        int textWidth = TextSplitUtil.getStringWidth(font, parseSubText.get(i));
                        // 计算居中位置
                        x -= textWidth / 2;
                    }
                    g2d.drawString(parseSubText.get(i), x + totalSubTextWidth, z);
                }
            }
        }
        return srcImg;
    }
}
