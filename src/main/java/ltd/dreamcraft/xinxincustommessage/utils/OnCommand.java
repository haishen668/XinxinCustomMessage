package ltd.dreamcraft.xinxincustommessage.utils;

import ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

/**
 * @author haishen668
 * @version 1.0
 * @description: TODO
 * @date 2024/4/2 13:30
 */
public class OnCommand implements Listener {
    public static int spigot = 0;

    public OnCommand() {
    }

    public static CommandSender getSender(long contactID, boolean isGroup) {
        CommandSender sender = null;
        if (spigot == 1) {
            sender = new ConsoleSenderLegacy(contactID, isGroup);
        }

        if (spigot == 2) {
            sender = new ConsoleSender(contactID, isGroup);
        }

        if (spigot == 0) {
            try {
                Class.forName("org.bukkit.command.CommandSender$Spigot");
            } catch (Exception var5) {
                spigot = 1;
                XinxinCustomMessage.getInstance().getLogger().info("§a检测到旧版本Minecraft,启用旧版本指令信息返回...");
                sender = new ConsoleSenderLegacy(contactID, isGroup);
            }

            if (sender == null) {
                spigot = 2;
                sender = new ConsoleSender(contactID, isGroup);
            }
        }

        return (CommandSender) sender;
    }


}
