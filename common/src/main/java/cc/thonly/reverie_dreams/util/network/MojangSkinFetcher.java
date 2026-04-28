package cc.thonly.reverie_dreams.util.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class MojangSkinFetcher {
    private static final Gson gson = new Gson();

    @SuppressWarnings("deprecation")
    public static JsonObject getSignedSkin(String usernameOrUuid) throws Exception {
        String uuid = usernameOrUuid.replace("-", "");

        if (!uuid.matches("^[0-9a-fA-F]{32}$")) {
            URI uuidUri = new URI("https://api.mojang.com/users/profiles/minecraft/" + usernameOrUuid);
            URL uuidUrl = uuidUri.toURL();

            HttpURLConnection uuidConn = (HttpURLConnection) uuidUrl.openConnection();
            uuidConn.setRequestMethod("GET");

            if (uuidConn.getResponseCode() != 200) {
                throw new IllegalArgumentException("玩家名不存在: " + usernameOrUuid);
            }

            JsonObject uuidJson = gson.fromJson(new InputStreamReader(uuidConn.getInputStream()), JsonObject.class);
            uuid = uuidJson.get("id").getAsString();
        }

        URL skinUrl = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
        HttpURLConnection skinConn = (HttpURLConnection) skinUrl.openConnection();
        skinConn.setRequestMethod("GET");

        if (skinConn.getResponseCode() != 200) {
            throw new IllegalStateException("获取皮肤失败（可能玩家没有改过皮肤或不是正版）");
        }

        JsonObject profileJson = gson.fromJson(new InputStreamReader(skinConn.getInputStream()), JsonObject.class);
        JsonArray properties = profileJson.getAsJsonArray("properties");

        for (JsonElement element : properties) {
            JsonObject prop = element.getAsJsonObject();
            if ("textures".equals(prop.get("name").getAsString())) {
                JsonObject result = new JsonObject();
                result.addProperty("id", profileJson.get("id").getAsString());
                result.addProperty("name", profileJson.get("name").getAsString());
                result.addProperty("value", prop.get("value").getAsString());
                result.addProperty("signature", prop.get("signature").getAsString());
                return result;
            }
        }

        throw new IllegalStateException("未找到皮肤信息");
    }
}
