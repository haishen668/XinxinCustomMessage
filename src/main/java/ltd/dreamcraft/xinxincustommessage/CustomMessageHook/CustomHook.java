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
        if ("invokeCounts_images".equalsIgnoreCase(identifier)) {
            return String.valueOf(DataManager.getCount("images"));
        }
        if ("invokeCounts_total".equalsIgnoreCase(identifier)) {
            return String.valueOf(DataManager.getCount("total"));
        }
        if ("invokeCounts_texts".equalsIgnoreCase(identifier)) {
            return String.valueOf(DataManager.getCount("total") - DataManager.getCount("images"));
        }


        return "";
    }

    public static String getUUID(String username) throws Exception {
        String url = "https://api.ashcon.app/mojang/v2/user/" + username;
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        try {
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);
                    return jsonObject.get("uuid").getAsString();
                }
            } else {
                throw new Exception("GET请求失败，响应码: " + responseCode);
            }
        } finally {
            con.disconnect();
        }
    }




}
