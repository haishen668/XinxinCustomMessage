package ltd.dreamcraft.xinxincustommessage.utils;

import com.xinxin.BotApi.BotAction;

/**
 * @author haishen668
 * @create-time 2024/7/25-10:25
 */
public class ScriptUtil {
    public static void execute(long groupId, String script) {
        String[] parts = script.split(":");
        if (parts.length > 1) {
            String command = parts[0].trim();
            String argument = parts[1].trim();
            switch (command) {
                case "msg":
                    BotAction.sendGroupMessage(groupId,argument,true);
                    break;
                // 可以添加更多的指令
                default:
                    BotAction.sendGroupMessage(groupId,"未知的命令格式,请联系管理员",true);; // 未知命令格式,请联系管理员
                    break;
            }
        } else {
            BotAction.sendGroupMessage(groupId,"无效的脚本格式,请联系管理员",true); // 无效的脚本格式,请联系管理员
        }
    }
}
