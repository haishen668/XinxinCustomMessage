package ltd.dreamcraft.xinxincustommessage.CustomMessageHook;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ltd.dreamcraft.xinxincustommessage.Managers.DataManager;
import ltd.dreamcraft.xinxincustommessage.XinxinCustomMessage;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
        // Mojang API 获取玩家 UUID
        if ("playeruuid".equalsIgnoreCase(identifier)) {
            try {
                return getUUID(player.getName());
            } catch (Exception e) {
                return "";
            }
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

    public static String getUUID(String username) throws Exception {
        // API URL
        String url = "https://api.ashcon.app/mojang/v2/user/" + username;

        // 创建一个URL对象
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // 设置请求类型为GET
        con.setRequestMethod("GET");

        // 获取响应码
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            // 读取响应内容
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // 使用 Gson 解析 JSON 响应
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);

            // 提取UUID
            String uuid = jsonObject.get("uuid").getAsString();

            return uuid;
        } else {
            throw new Exception("GET请求失败，响应码: " + responseCode);
        }
    }




}
