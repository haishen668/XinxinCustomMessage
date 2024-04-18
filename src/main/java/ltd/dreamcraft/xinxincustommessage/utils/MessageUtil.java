package ltd.dreamcraft.xinxincustommessage.utils;


import com.xinxin.BotApi.BotAction;
import com.xinxin.BotApi.BotBind;
import ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage;
import ltd.dreamcraft.xinxincustommessage.objects.CustomImage;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MessageUtil {
    public static List<String> getMsg(List<String> message, OfflinePlayer offlinePlayer, long groupID, long userID, String nick, String extra) {
        List<String> response = new ArrayList<>();
        for (String s : message) {
            String name = (offlinePlayer != null) ? offlinePlayer.getName() : nick;
            s = s.replace("{qq}", String.valueOf(userID));
            if (name!=null)
                s = s.replace("{nick}", name);
            s = s.replace("{extra}", extra);
            try {
                s = PlaceholderAPI.setPlaceholders(offlinePlayer, s);
            } catch (IllegalStateException e) {
                if (XinxinCustomMessage.getInstance().getConfig().getBoolean("debug")) {
                    e.printStackTrace();
                } else {
                    XinxinCustomMessage.getInstance().getLogger().warning("字符串： " + s + "中的变量无法正常解析，可能变量无法异步获取，请打开debug查看详细信息");
                }
            }
            if (s.startsWith("[image]")) {
                String imageID = s.replace("[image]", "").trim();
                try {
                    CustomImage customImage = null;
                    for (CustomImage customImage1 : XinxinCustomMessage.customImageList) {
                        if (customImage1.id.equalsIgnoreCase(imageID))
                            customImage = customImage1;
                    }
                    if (customImage == null) {
                        response.add("无法找到图片ID: " + imageID);
                        continue;
                    }
                    //渲染图片
                    BufferedImage image = customImage.renderImage(offlinePlayer);
                    //发送信息
//                    System.out.println(bufferedImgToMsg(image));
//                    return Collections.singletonList(bufferedImgToMsg(image));
                    response.add(bufferedImgToMsg(image));
                } catch (Exception e) {
                    if (XinxinCustomMessage.getInstance().getConfig().getBoolean("debug"))
                        e.printStackTrace();
                }
                continue;
            }
            if (s.startsWith("[command]")) {
                String cmd = s.replace("[command]", "").trim();
                CommandSender commandSender = OnCommand.getSender(groupID, true);
                Bukkit.getScheduler().runTask(XinxinCustomMessage.getInstance(), () -> Bukkit.dispatchCommand(commandSender, cmd));
                continue;
            }
            response.add(s);
        }
        return response;
    }

    public static String bufferedImgToMsg(BufferedImage image) {
        return getImageMsg("base64://" + imageToBase64(image));
    }
    public static String getImageMsg(String file) {
        file = formatCQCode(file);
        return "[CQ:image,file=" + file + "]";
    }
    public static String formatCQCode(String str) {
        return str.replace("&", "&amp;").replace("[", "&#91;").replace("]", "&#93;").replace(",", "&#44;");
    }
    public static String imageToBase64(BufferedImage image) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            ImageIO.write(image, "png", os);
            return Base64.getEncoder().encodeToString(os.toByteArray());
        } catch (IOException var3) {
            throw new UncheckedIOException(var3);
        }
    }
    public static void sendMessage(List<String> message, long groupID, long userID, String nick, String extra) {
        OfflinePlayer offlinePlayer = null;
//        UUID uuid = Bot.getApi().getPlayer(userID);
        String bindPlayerName = BotBind.getBindPlayerName(String.valueOf(userID));
        if (bindPlayerName != null) {
            offlinePlayer = Bukkit.getOfflinePlayer(bindPlayerName);
            if (offlinePlayer.getName() == null)
                offlinePlayer = null;
        }
        List<String> response = getMsg(message, offlinePlayer, groupID, userID, nick, extra);
        if (!response.isEmpty())
            BotAction.sendGroupMessage(Math.toIntExact(groupID), response, false);
    }

}


