package ltd.dreamcraft.xinxincustommessage.objects;

import com.xinxin.BotApi.BotBind;
import io.github.ParsePokemon;
import ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage;
import ltd.dreamcraft.xinxincustommessage.utils.TextUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author haishen668
 */
public class CustomImage implements Cloneable, Serializable {
    // 图片的唯一标识符
    public final String id;

    // 图片的来源路径
    public String source;

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

    public static BufferedImage downloadImage(String link) {
        InputStream in = null;
        try {
            URL url = new URL(link);
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(20000);
            connection.setReadTimeout(20000);
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0");
            connection.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            connection.addRequestProperty("Pragma", "no-cache");
            in = connection.getInputStream();
            return ImageIO.read(in);
        } catch (Exception e) {
            if (XinxinCustomMessage.getInstance().getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
            return null;
        } finally {
            if (in != null) {
                try { in.close(); } catch (IOException ignored) {}
            }
        }
    }


    // 深度克隆方法
    @Override
    public CustomImage clone() {
        return this.deepClone();
    }

    public CustomImage deepClone() {
        try {
            // Serialize the object to a byte array
            ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();
            ObjectOutputStream outStream = new ObjectOutputStream(byteOutStream);
            outStream.writeObject(this);
            outStream.flush();

            // Deserialize the byte array back into an object
            ByteArrayInputStream byteInStream = new ByteArrayInputStream(byteOutStream.toByteArray());
            ObjectInputStream inStream = new ObjectInputStream(byteInStream);
            return (CustomImage) inStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    // 调整图片大小
    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        Image resultingImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = outputImage.createGraphics();
        outputImage = g.getDeviceConfiguration().createCompatibleImage(targetWidth, targetHeight, Transparency.TRANSLUCENT);
        g.dispose();
        g = outputImage.createGraphics();
        g.drawImage(resultingImage, 0, 0, null);
        g.dispose();
        return outputImage;
    }

    public BufferedImage renderImage(OfflinePlayer player, String extra) throws Exception {
        BufferedImage srcImg;

        String path = this.source;
        boolean hasExtra = extra != null && !extra.isEmpty();

        if (hasExtra) {
            path = path.replaceAll("\\{extra}", extra);
        }

        if (player != null) {
            path = PlaceholderAPI.setPlaceholders(player, path);
            String qq = BotBind.getBindQQ(player.getName());
            if (qq != null) {
                path = path.replace("{qq}", qq.toLowerCase());
            }
        }

        if (path.startsWith("[url]")) {
            srcImg = downloadImage(path.substring(5));
        } else {
            File imageFolder = new File(XinxinCustomMessage.getInstance().getDataFolder(), "images");
            File imageFile = new File(imageFolder, path);
            srcImg = ImageIO.read(imageFile);
        }

        if (srcImg == null) {
            throw new Exception("无法加载图片: " + path);
        }

        if (Math.min(this.width, this.height) > 0) {
            srcImg = resizeImage(srcImg, this.width, this.height);
        }

        Graphics2D g2d = srcImg.createGraphics();
        g2d.drawImage(srcImg, 0, 0, srcImg.getWidth(), srcImg.getHeight(), null);

        try {
            for (SubImage subImage : this.subImages) {
                BufferedImage image = null;
                String subImgPath = subImage.path;
                if (hasExtra) {
                    subImgPath = subImgPath.replaceAll("\\{extra}", extra);
                }

                if (player != null) {
                    subImgPath = PlaceholderAPI.setPlaceholders(player, subImgPath);
                }

                if (subImgPath.startsWith("[url]")) {
                    if (player != null) {
                        String qq = BotBind.getBindQQ(player.getName());
                        if (qq != null) {
                            subImgPath = subImgPath.replace("{qq}", qq.toLowerCase());
                        }
                    }
                    image = downloadImage(subImgPath.substring(5));
                } else if (subImgPath.startsWith("[pokemon]")) {
                    String imageFolder = "plugins/XinxinCustomMessage/images/pokemonImg";
                    if (player != null) {
                        try {
                            image = ParsePokemon.pokemonToImg(imageFolder, subImgPath, player);
                        } catch (NoClassDefFoundError e) {
                            Bukkit.getConsoleSender().sendMessage("§f[§e" + XinxinCustomMessage.getInstance().getName() + "§f]§c " + "发生错误!,请检查PokemonBag的版本是否为最新");
                        }
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
                if (image != null) {
                    if (Math.min(w, h) > 0) {
                        image = resizeImage(image, w, h);
                    }
                    if (subImage.center) {
                        x -= image.getWidth() / 2;
                        z -= image.getHeight() / 2;
                    }
                    g2d.drawImage(image, x, z, null);
                }
            }

            for (CustomText customText : this.customTexts) {
                String text = customText.text;

                if (hasExtra) {
                    text = text.replaceAll("\\{extra}", extra);
                }

                try {
                    if (player != null) {
                        text = PlaceholderAPI.setPlaceholders(player, text);
                        String qq = BotBind.getBindQQ(player.getName());
                        if (qq != null) {
                            text = text.replace("{qq}", qq);
                        }
                    }
                } catch (IllegalStateException e) {
                    if (XinxinCustomMessage.getInstance().getConfig().getBoolean("debug")) {
                        e.printStackTrace();
                    } else {
                        XinxinCustomMessage.getInstance().getLogger().warning("字符串: " + text + "中的变量无法正常解析,可能变量无法异步获取,请打开debug查看详细信息");
                    }
                }

                if (text.contains("[pokemon]")) {
                    if (player != null) {
                        try {
                            text = ParsePokemon.pokemonToString(text, player);
                        } catch (NoClassDefFoundError e) {
                            Bukkit.getConsoleSender().sendMessage("§f[§e" + XinxinCustomMessage.getInstance().getName() + "§f]§c " + "发生错误!,请检查PokemonBag的版本是否为最新");
                        }
                    }
                    if (text == null) {
                        continue;
                    }
                }

                int x = customText.x;
                int z = customText.z;
                Font font = customText.font;
                boolean center = customText.center;

                ArrayList<String> subString = TextUtil.split(text);
                ArrayList<String> parseSubText = new ArrayList<>();

                if (subString.isEmpty()) {
                    Color color = TextUtil.parseColor(TextUtil.getParseString(text, "color"));
                    if (color == null) color = Color.BLACK;
                    g2d.setFont(font);
                    g2d.setColor(color);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    if (center) {
                        int textWidth = TextUtil.getStringWidth(font, TextUtil.getParseString(text, "string"));
                        x -= textWidth / 2;
                    }
                    g2d.drawString(TextUtil.getParseString(text, "string"), x, z);
                } else {
                    if (center) {
                        String totalText = TextUtil.getParseString(text, "string");
                        int totalTextWidth = TextUtil.getStringWidth(font, totalText);
                        x -= totalTextWidth / 2;
                    }
                    int totalSubTextWidth = 0;
                    for (int i = 0; i < subString.size(); i++) {
                        String subText = subString.get(i);
                        Color color = TextUtil.parseColor(TextUtil.getParseString(subText, "color"));
                        if (color == null) color = Color.BLACK;
                        if (i - 1 >= 0) {
                            totalSubTextWidth += TextUtil.getStringWidth(font, parseSubText.get(i - 1));
                        }
                        parseSubText.add(TextUtil.getParseString(subText, "string"));
                        g2d.setFont(font);
                        g2d.setColor(color);
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.drawString(parseSubText.get(i), x + totalSubTextWidth, z);
                    }
                }
            }
        } finally {
            g2d.dispose();
        }
        return srcImg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) return false;
        CustomImage that = (CustomImage) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
