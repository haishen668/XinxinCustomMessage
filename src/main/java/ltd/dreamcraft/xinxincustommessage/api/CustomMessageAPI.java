package ltd.dreamcraft.xinxincustommessage.api;

import com.xinxin.BotApi.BotAction;
import com.xinxin.BotApi.BotBind;
import com.xinxin.GroupData.GroupMemberInfo;
import ltd.dreamcraft.xinxincustommessage.objects.CustomMessage;
import ltd.dreamcraft.xinxincustommessage.utils.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.List;

import static ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage.customMessageList;

/**
 * @Author: haishen668
 * @CreateTime: 2024-05-06
 * @Description: 提供了一些开放的api
 * @Version: 1.0
 */
public class CustomMessageAPI {
    public static boolean sendCustomMessage(Long GroupId, String playerName, String messageId, String extra) {
        long id;
        CustomMessage message = null;
        for (CustomMessage customMessage : customMessageList) {
            if (customMessage.getId().equalsIgnoreCase(messageId)) {
                message = customMessage;
                break;
            }
        }
        if (message == null) {
            System.out.println("§c消息ID: §b" + messageId + " §c不存在!");
            return false;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        try {
            id = GroupId;
        } catch (Exception ignored) {
            System.out.println("§a群号必须为数字!");
            return false;
        }
        String userID = BotBind.getBindQQ(playerName);
        if (userID == null || userID.isEmpty()) {
            userID = "0";
        }
        String nickName = "";
        if (!userID.equals("0")) {
            GroupMemberInfo groupMemberInfo = BotAction.getGroupMemberInfo(id, Long.parseLong(userID), true);
            nickName = groupMemberInfo.getNickname();
        }

        List<String> response = MessageUtil.getMsg(message.getResponses(), player, id, Long.parseLong(userID), nickName, extra);
        if (!response.isEmpty())
            BotAction.sendGroupMessage((int) id, response, true);
        return true;
    }

}


