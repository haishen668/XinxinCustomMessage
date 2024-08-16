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

        for (CustomMessage customMessage : XinxinCustomMessage.customMessageList) {//遍历所有信息
            if (customMessage.groups.isEmpty() || customMessage.groups.contains(event.getGroup_id())) {//是否在监听列表当中
                boolean regex = false;  //标记：是否是正则表达式
                if (customMessage.trigger.startsWith("[regex]")) { //是否以正则表达式 开头
                    String pattern = customMessage.trigger.replace("[regex]", "").trim();
                    Matcher matcher = Pattern.compile(pattern).matcher(event.getMessage());
                    if (matcher.find())
                        regex = true;
                }
                // 新增一个图片识别
                // if (customMessage.trigger.startsWith("[image]") && XinxinCustomMessage.getInstance().getConfig().getBoolean("ocr-scan")){
                //     String message = customMessage.trigger.replace("[image]", "").trim();
                //     String eventImgOCR = event.getImgOCR();
                //     if (eventImgOCR.contains(message) || eventImgOCR.replace(" ","").toLowerCase().contains(message.toLowerCase())){
                //         MessageUtil.sendMessage(customMessage.responses, event.getGroup_id(), event.getUser_id(), event.getSender().getName(),"");
                //         continue;
                //     }
                //
                if (event.getMessage().equalsIgnoreCase(customMessage.trigger) || regex || customMessage.trigger.contains("{extra}")) {
                    //信息和关键词完全匹配 || 符合正则表达式 || (是否包含{extra} => 关键词中包含触发器)
                    String message = event.getMessage();
                    String trigger = customMessage.trigger;

                    if (trigger.contains("{extra}")) {  //
                        //信息要包含除了{extra}的每个子字符串 不然就返回
                        String[] triggerArg = trigger.split("\\{extra}");
                        boolean allSubMessagesMatched = true;

                        for (String subMessage : triggerArg) {
                            if (!message.contains(subMessage)) {
                                allSubMessagesMatched = false;
                                break;
                            }
                            message = message.replace(subMessage, "");
                        }

                        if (!allSubMessagesMatched) {
                            continue; // 跳过当前的customMessage，进行下一次外层循环
                        }
                    }

                    if (customMessage.admins.isEmpty() || customMessage.admins.contains(event.getUser_id())) {
                        String bindPlayerName = BotBind.getBindPlayerName(String.valueOf(event.getUser_id()));
                        String extra = !regex ? message : ""; //正则没匹配到就返回 message
                        if (customMessage.unbind_messages.isEmpty() || bindPlayerName != null) {
                            MessageUtil.sendMessage(customMessage.responses, event.getGroup_id(), event.getUser_id(), bindPlayerName, extra);
                            continue;
                        }
                        MessageUtil.sendMessage(customMessage.unbind_messages, event.getGroup_id(), event.getUser_id(), bindPlayerName, extra);
                    } else {
                        BotAction.sendGroupMessage(event.getGroup_id(), "你没有权限使用该指令", true);
                    }
                }
            }
        }
    }

}
