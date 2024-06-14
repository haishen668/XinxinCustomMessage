package ltd.dreamcraft.xinxincustommessage.listeners;


import com.xinxin.BotApi.BotAction;
import com.xinxin.BotApi.BotBind;
import com.xinxin.BotEvent.GroupMessageEvent;
import com.xinxin.BotEvent.GroupUserChangesEvent;
import ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage;
import ltd.dreamcraft.xinxincustommessage.objects.CustomMessage;
import ltd.dreamcraft.xinxincustommessage.utils.MessageUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageListener implements Listener {
    @EventHandler
    public void onGroupMemberIncrease(GroupUserChangesEvent event) {
        for (CustomMessage customMessage : XinxinCustomMessage.customMessageList) {
            if ((customMessage.groups.isEmpty() || customMessage.groups.contains(event.getGroup_id())) &&
                    "[join]".equalsIgnoreCase(customMessage.trigger)
                    && event.isIncrease()) {
                if (customMessage.unbind_messages.isEmpty()) {
                    MessageUtil.sendMessage(customMessage.responses, event.getGroup_id(), event.getUser_id(), "", "");
                    continue;
                }
                MessageUtil.sendMessage(customMessage.unbind_messages, event.getGroup_id(), event.getUser_id(), "", "");
            }


            if ((customMessage.groups.isEmpty() || customMessage.groups.contains(event.getGroup_id())) &&
                    "[leave]".equalsIgnoreCase(customMessage.trigger)
                    && !event.isIncrease()) {
                String bindPlayerName = BotBind.getBindPlayerName(String.valueOf(event.getUser_id()));
                if (customMessage.unbind_messages.isEmpty() || bindPlayerName != null) {
                    MessageUtil.sendMessage(customMessage.responses, event.getGroup_id(), event.getUser_id(), "", "");
                    continue;
                }
                MessageUtil.sendMessage(customMessage.unbind_messages, event.getGroup_id(), event.getUser_id(), "", "");
            }
        }
    }


    @EventHandler
    public void onMsg(GroupMessageEvent event) {
        if (XinxinCustomMessage.LOG)
            XinxinCustomMessage.getInstance().getLogger().info("§a群[" + event.getGroup_id() + "]: §b" + event.getMessage());
        for (CustomMessage customMessage : XinxinCustomMessage.customMessageList) {
            if (customMessage.groups.isEmpty() || customMessage.groups.contains(event.getGroup_id())) {
                boolean regex = false;
                if (customMessage.trigger.startsWith("[regex]")) {
                    String pattern = customMessage.trigger.replace("[regex]", "").trim();
                    Matcher matcher = Pattern.compile(pattern).matcher(event.getMessage());
                    if (matcher.find())
                        regex = true;
                }
                //新增一个图片识别
//                if (customMessage.trigger.startsWith("[image]") && XinxinCustomMessage.getInstance().getConfig().getBoolean("ocr-scan")){
//                    String message = customMessage.trigger.replace("[image]", "").trim();
//                    String eventImgOCR = event.getImgOCR();
//                    if (eventImgOCR.contains(message) || eventImgOCR.replace(" ","").toLowerCase().contains(message.toLowerCase())){
//                        MessageUtil.sendMessage(customMessage.responses, event.getGroup_id(), event.getUser_id(), event.getSender().getName(),"");
//                        continue;
//                    }
//                }
                if (event.getMessage().equalsIgnoreCase(customMessage.trigger) || regex || (customMessage.trigger
                        .contains("{extra}") && event.getMessage().startsWith(customMessage.trigger.replace("{extra}", "")))) {
                    if (customMessage.admins.isEmpty() || customMessage.admins.contains(event.getUser_id())) {
                        String bindPlayerName = BotBind.getBindPlayerName(String.valueOf(event.getUser_id()));
                        String extra = !regex ? event.getMessage().substring(customMessage.trigger.replace("{extra}", "").length() - 1).trim() : "";
                        if (customMessage.unbind_messages.isEmpty() || bindPlayerName != null) {
                            MessageUtil.sendMessage(customMessage.responses, event.getGroup_id(), event.getUser_id(), BotBind.getBindPlayerName(String.valueOf(event.getUser_id())), extra);
                            continue;
                        }
                        MessageUtil.sendMessage(customMessage.unbind_messages, event.getGroup_id(), event.getUser_id(), BotBind.getBindPlayerName(String.valueOf(event.getUser_id())), extra);
                    } else {
                        BotAction.sendGroupMessage(event.getGroup_id(), "你没有权限使用该指令", true);
                    }
                }
            }
        }
    }
}
