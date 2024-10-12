package ltd.dreamcraft.xinxincustommessage.utils;

import com.xinxin.BotApi.BotAction;
import org.bukkit.Bukkit;

import java.util.Arrays;

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
                case "message":
                case "msg":
                    BotAction.sendGroupMessage(groupId, argument, true);
                    break;
                case "command":
                case "cmd":
                    // 通过控制台身份执行命令
                    if (argument.trim().startsWith("[") && argument.endsWith("]")) {
                        // 移除方括号并分割成命令数组
                        String[] commandArgs = argument.substring(1, argument.length() - 1).split(",");
                        // 如果有多个子命令，逐个执行
                        Arrays.stream(commandArgs).forEach(subCommand ->
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), subCommand.trim())
                        );
                    } else {
                        // 如果不是数组，直接执行单个命令
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), argument.trim());
                    }
                    break;
                // 可以添加更多的指令
                default:
                    BotAction.sendGroupMessage(groupId, "未知的命令格式,请联系管理员", true); // 未知命令格式,请联系管理员
                    break;
            }
        } else {
            BotAction.sendGroupMessage(groupId, "无效的脚本格式,请联系管理员", true); // 无效的脚本格式,请联系管理员
        }
    }
}
