package ltd.dreamcraft.xinxincustommessage.CustomMessageHook;

import ltd.dreamcraft.xinxincustommessage.Managers.DataManager;
import ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

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
        if (identifier.equalsIgnoreCase("playerIsOnline")) {
            return player.isOnline() ? "&#26E78B在线" : "&#FF647E离线";
        }
        //图片模板调用次数
        if (identifier.equalsIgnoreCase("invokeCounts_images")) {
            Integer images = DataManager.invokeCountsMap.get("images");
            return String.valueOf(images);
        }
        //模板总调用次数
        if (identifier.equalsIgnoreCase("invokeCounts_total")) {
            Integer total = DataManager.invokeCountsMap.get("total");
            return String.valueOf(total);
        }
        //文本模板调用次数
        if (identifier.equalsIgnoreCase("invokeCounts_texts")) {
            Integer texts = DataManager.invokeCountsMap.get("total") - DataManager.invokeCountsMap.get("images");
            return String.valueOf(texts);
        }


        return "";
    }


}
