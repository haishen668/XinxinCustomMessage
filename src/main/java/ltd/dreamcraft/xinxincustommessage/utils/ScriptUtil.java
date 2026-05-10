package ltd.dreamcraft.xinxincustommessage.utils;

import com.xinxin.BotApi.BotAction;
import ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage;
import org.bukkit.Bukkit;

/**
 * @author haishen668
 * @create-time 2024/7/25-10:25
 */
public class ScriptUtil {
    public static void execute(long groupId, String script) {
        int colonIndex = script.indexOf(':');
        if (colonIndex > 0) {
            String command = script.substring(0, colonIndex).trim();
            String argument = script.substring(colonIndex + 1).trim();
            switch (command) {
                case "message":
                case "msg":
                    BotAction.sendGroupMessage(groupId, argument, true);
                    break;
                case "command":
                case "cmd":
                    if (argument.startsWith("[") && argument.endsWith("]")) {
                        String[] commandArgs = argument.substring(1, argument.length() - 1).split(",");
                        Bukkit.getScheduler().runTask(XinxinCustomMessage.getInstance(), () -> {
                            for (String subCommand : commandArgs) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), subCommand.trim());
                            }
                        });
                    } else {
                        Bukkit.getScheduler().runTask(XinxinCustomMessage.getInstance(), () ->
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), argument));
                    }
                    break;
                default:
                    BotAction.sendGroupMessage(groupId, "未知的命令格式,请联系管理员", true);
                    break;
            }
        } else {
            BotAction.sendGroupMessage(groupId, "无效的脚本格式,请联系管理员", true);
        }
    }
}
