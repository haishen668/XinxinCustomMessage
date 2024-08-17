package ltd.dreamcraft.xinxincustommessage.CustomMessageHook;

import ltd.dreamcraft.xinxincustommessage.Managers.DataManager;
import ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * @Author: haishen668
 * @CreateTime: 2024-05-07
 * @Description: 自定义占位符
 * @Version: 1.0
 */
public class CustomHook extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "bot";
    }

    @Override
    public @NotNull String getAuthor() {
        return "haishen668";
    }

    @Override
    public @NotNull String getVersion() {
        return XinxinCustomMessage.getInstance().getDescription().getVersion();
    }

    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null) {
            return "";
        }
        //在线状态
        if ("playerIsOnline".equalsIgnoreCase(identifier)) {
            return player.isOnline() ? "&#26E78B在线" : "&#FF647E离线";
        }
        //玩家离线uuid
        if ("playeruuid".equalsIgnoreCase(identifier)) {
            UUID uniqueId = player.getUniqueId();
            return uniqueId.toString();
        }
        //图片模板调用次数
        if ("invokeCounts_images".equalsIgnoreCase(identifier)) {
            Integer images = DataManager.invokeCountsMap.get("images");
            return String.valueOf(images);
        }
        //模板总调用次数
        if ("invokeCounts_total".equalsIgnoreCase(identifier)) {
            Integer total = DataManager.invokeCountsMap.get("total");
            return String.valueOf(total);
        }
        //文本模板调用次数
        if ("invokeCounts_texts".equalsIgnoreCase(identifier)) {
            Integer texts = DataManager.invokeCountsMap.get("total") - DataManager.invokeCountsMap.get("images");
            return String.valueOf(texts);
        }


        return "";
    }


}
