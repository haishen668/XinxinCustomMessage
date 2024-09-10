package ltd.dreamcraft.xinxincustommessage.listeners;


import com.xinxin.BotApi.BotAction;
import com.xinxin.BotApi.BotBind;
import com.xinxin.BotEvent.GroupMessageEvent;
import com.xinxin.BotEvent.GroupUserChangesEvent;
import ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage;
import ltd.dreamcraft.xinxincustommessage.objects.CustomMessage;
import ltd.dreamcraft.xinxincustommessage.utils.MessageUtil;
import ltd.dreamcraft.xinxincustommessage.utils.ScriptUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.List;
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
            // 检查信息触发群 是否在监听的列表中
            if (!customMessage.groups.isEmpty() && !customMessage.groups.contains(event.getGroup_id())) {
                continue;
            }
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

                if (trigger.contains("{extra}")) {
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
                        if (customMessage.scripts.isEmpty()) {
                            MessageUtil.sendMessage(customMessage.responses, event.getGroup_id(), event.getUser_id(), bindPlayerName, extra);
                            return;
                        }else {
                            ScriptEngine scriptEngine = XinxinCustomMessage.getScriptEngine();
                            List<String> scripts = customMessage.getScripts();
                            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(bindPlayerName);
                            for (String script : scripts) {
                                // 替换{extra}占位符
                                script = script.replaceAll("\\{extra}",extra);
                                script = PlaceholderAPI.setPlaceholders(offlinePlayer, script); // 解析papi变量 和
                                // script == %player_level% >= 50 -> msg:&7您需到达50级才可领取本邮件
                                String[] splitScript = script.split("->");
                                if (splitScript.length > 1) {
                                    String conditionScript = splitScript[0];
                                    try {
                                        Object evaluationResult = scriptEngine.eval(conditionScript);
                                        if (evaluationResult instanceof Boolean) {
                                            boolean conditionMet = (boolean) evaluationResult;
                                            if (!conditionMet) {
                                                // 条件不符合 执行下方语句
                                                String executeScript = splitScript[1];
                                                // 使用自制脚本解析器解析
                                                ScriptUtil.execute(event.getGroup_id(),executeScript);
                                                return;
                                            }
                                        }
                                    } catch (ScriptException e) {
                                        BotAction.sendGroupMessage(event.getGroup_id(),"脚本解析异常",true);
                                        e.printStackTrace();
                                        return;
                                    }
                                }
                            }
                            MessageUtil.sendMessage(customMessage.responses, event.getGroup_id(), event.getUser_id(), bindPlayerName, extra);
                            return;
                        }
                    }
                    MessageUtil.sendMessage(customMessage.unbind_messages, event.getGroup_id(), event.getUser_id(), null, extra);
                } else {
                    BotAction.sendGroupMessage(event.getGroup_id(), "你没有权限使用该指令", true);
                }
                break; // 退出循环，因为已经找到匹配的消息
            }
        }
    }

}
