package ltd.dreamcraft.xinxincustommessage.utils;

import ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage;

import java.awt.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工具类，用于处理文本和颜色相关的操作。
 *
 * @author haishen668
 */
public class TextUtil {

    /**
     * 将带有颜色代码的文本拆分成多个子文本片段。
     *
     * @param text 原始文本
     * @return 子文本列表
     */
    public static ArrayList<String> split(String text) {
        // 将Minecraft颜色代码替换为Minecraft接受的颜色代码
        text = text.replace("§", "&");
        ArrayList<String> subText = new ArrayList<>();
        // 正则表达式，用于匹配颜色代码和其后的文本
        String regex = "(&#[0-9A-Fa-f]{6}|&[0-9A-Fa-f])[^&]*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            subText.add(matcher.group());
        }
        // 如果没有匹配项，将整个文本作为一个子文本
        if (subText.isEmpty()) {
            subText.add(text);
        }
        return subText;
    }

    /**
     * 根据模式解析字符串，支持"string"和"color"两种模式。
     *
     * @param str  需要解析的字符串
     * @param mode 解析模式，"string"或"color"
     * @return 解析后的字符串或颜色代码
     */
    public static String getParseString(String str, String mode) {
        // 定义用于匹配颜色代码的正则表达式
        String regex = "&#[0-9A-Fa-f]{6}|&[0-9A-Fa-f]";
        if ("string".equalsIgnoreCase(mode)) {
            // 如果模式为"string"，移除所有颜色代码
            return str.replaceAll(regex, "");
        } else if ("color".equalsIgnoreCase(mode)) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                // 如果模式为"color"，返回第一个匹配的颜色代码
                return matcher.group();
            }
        }
        return null; // 如果模式不正确或未找到匹配项，返回null
    }

    /**
     * 将十六进制颜色码转换为Color对象。
     *
     * @param colorCode 十六进制颜色码
     * @return Color对象，或在颜色码无效时返回null
     */
    public static Color parseColor(String colorCode) {
        if (colorCode != null) {
            if (colorCode.startsWith("&#")) {
                // 处理十六进制颜色码
                colorCode = colorCode.replace("&#", "");
                try {
                    int intValue = Integer.parseInt(colorCode, 16);
                    return new Color(intValue);
                } catch (NumberFormatException e) {
                    XinxinCustomMessage.getInstance().getLogger().severe("无效的颜色码");
                }
            } else if (colorCode.startsWith("&")) {
                // 处理Minecraft颜色代码
                switch (colorCode) {
                    case "&0":
                        return Color.BLACK;
                    case "&1":
                        return Color.BLUE;
                    case "&2":
                        return Color.GREEN;
                    case "&3":
                        return new Color(0, 170, 170);
                    case "&4":
                        return Color.RED;
                    case "&5":
                        return new Color(170, 0, 170);
                    case "&6":
                        return new Color(255, 170, 0);
                    case "&7":
                        return new Color(170, 170, 170);
                    case "&8":
                        return new Color(85, 85, 85);
                    case "&9":
                        return new Color(85, 85, 255);
                    case "&a":
                        return new Color(85, 255, 85);
                    case "&b":
                        return new Color(85, 255, 255);
                    case "&c":
                        return new Color(255, 85, 85);
                    case "&d":
                        return new Color(255, 85, 255);
                    case "&e":
                        return new Color(255, 255, 85);
                    case "&f":
                        return new Color(255, 255, 255);
                    default:
                        break;
                }
            }
        }
        XinxinCustomMessage.getInstance().getLogger().severe("无效的颜色码");
        return null;
    }

    /**
     * 获取字符串的宽度。
     *
     * @param font    字体
     * @param content 字符串
     * @return 字符串的宽度
     */
    public static int getStringWidth(Font font, String content) {
        FontMetrics metrics = Toolkit.getDefaultToolkit().getFontMetrics(font);
        return metrics.stringWidth(content);
    }
}