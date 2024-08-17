package ltd.dreamcraft.xinxincustommessage.utils;

import com.xinxin.BotApi.BotAction;
import ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;

/**
 * @author haishen668
 * @version 1.0
 * @description: TODO
 * @date 2024/4/2 13:35
 */
public class ConsoleSender implements ConsoleCommandSender {
    private final long contactID;
    private final boolean isGroup;
    private final ArrayList<String> output = new ArrayList<>();
    private final ArrayList<String> tempOutPut = new ArrayList<>();
    private final ConsoleSender instance;
    private BukkitTask task = null;

    public ConsoleSender(long contactID, boolean isGroup) {
        this.contactID = contactID;
        this.isGroup = isGroup;
        this.instance = this;
    }

    private Optional<ConsoleCommandSender> get() {
        return Optional.of(Bukkit.getServer().getConsoleSender());
    }

    public @NotNull Server getServer() {
        return Bukkit.getServer();
    }

    public @NotNull String getName() {
        return "CONSOLE";
    }

    public void sendMessage(@NotNull String message) {
        if (this.task != null) {
            this.task.cancel();
        }

        synchronized (this.tempOutPut) {
            this.tempOutPut.add(message);
        }

        this.task = Bukkit.getScheduler().runTaskLaterAsynchronously(XinxinCustomMessage.getInstance(), () -> {
            synchronized (this.output) {
                synchronized (this.tempOutPut) {
                    this.output.addAll(this.tempOutPut);
                    this.tempOutPut.clear();
                }

                StringBuilder response = new StringBuilder();
                Iterator var3 = this.output.iterator();

                while (var3.hasNext()) {
                    String s = (String) var3.next();
                    response.append(s.replaceAll("§\\S", "")).append("\n");
                }

                String msg = response.toString().trim();
                if (!msg.isEmpty()) {
                    if (this.isGroup) {
                        BotAction.sendGroupMessage(this.contactID, msg, true);
                    } else {
                        BotAction.sendPrivateMessage(this.contactID, msg, true);
                    }

                    this.output.clear();
                }

            }
        }, 4L);
    }

    public void sendMessage(String[] messages) {
        String[] var2 = messages;
        int var3 = messages.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            String msg = var2[var4];
            this.sendMessage(msg);
        }

    }

    public boolean isPermissionSet(@NotNull String s) {
        return (Boolean) this.get().map((c) -> {
            return c.isPermissionSet(s);
        }).orElse(true);
    }

    public boolean isPermissionSet(@NotNull Permission permission) {
        return (Boolean) this.get().map((c) -> {
            return c.isPermissionSet(permission);
        }).orElse(true);
    }

    public boolean hasPermission(@NotNull String s) {
        return (Boolean) this.get().map((c) -> {
            return c.hasPermission(s);
        }).orElse(true);
    }

    public boolean hasPermission(@NotNull Permission permission) {
        return (Boolean) this.get().map((c) -> {
            return c.hasPermission(permission);
        }).orElse(true);
    }

    public boolean isOp() {
        return true;
    }

    public void setOp(boolean b) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public CommandSender. Spigot spigot() {
        return new CommandSender.Spigot() {
            public void sendMessage(BaseComponent component) {
                ConsoleSender.this.instance.sendMessage(component.toPlainText());
            }

            public void sendMessage(BaseComponent... components) {
                BaseComponent[] var2 = components;
                int var3 = components.length;

                for (int var4 = 0; var4 < var3; ++var4) {
                    BaseComponent baseComponent = var2[var4];
                    this.sendMessage(baseComponent);
                }

            }
        };
    }

    public boolean isConversing() {
        throw new UnsupportedOperationException();
    }

    public void acceptConversationInput(@NotNull String s) {
    }

    public boolean beginConversation(@NotNull Conversation conversation) {
        throw new UnsupportedOperationException();
    }

    public void abandonConversation(@NotNull Conversation conversation) {
        throw new UnsupportedOperationException();
    }

    public void abandonConversation(@NotNull Conversation conversation, @NotNull ConversationAbandonedEvent conversationAbandonedEvent) {
        throw new UnsupportedOperationException();
    }

    public void sendRawMessage(@NotNull String s) {
    }

    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b) {
        throw new UnsupportedOperationException();
    }

    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        throw new UnsupportedOperationException();
    }

    public PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String s, boolean b, int i) {
        throw new UnsupportedOperationException();
    }

    public PermissionAttachment addAttachment(@NotNull Plugin plugin, int i) {
        throw new UnsupportedOperationException();
    }

    public void removeAttachment(@NotNull PermissionAttachment permissionAttachment) {
        throw new UnsupportedOperationException();
    }

    public void recalculatePermissions() {
        throw new UnsupportedOperationException();
    }

    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        throw new UnsupportedOperationException();
    }
}
