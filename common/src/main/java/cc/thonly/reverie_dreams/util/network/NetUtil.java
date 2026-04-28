package cc.thonly.reverie_dreams.util.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NetUtil {
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();

    public static boolean isUrlAccessible(String url) {
        Request request = new Request.Builder()
                .url(url)
                .head()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    public static void downloadFile(String url, File destFile) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            ResponseBody body = response.body();
            if (body == null) {
                throw new IOException("Response body is null");
            }

            try (InputStream in = body.byteStream();
                 FileOutputStream out = new FileOutputStream(destFile)) {

                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
            }
        }
    }


    public static boolean isChinaNetwork() {
        try {
            Request req = new Request.Builder().url("http://ip-api.com/json/").build();
            Response resp = client.newCall(req).execute();
            String body = resp.body().string();
            JsonObject object = gson.fromJson(body, JsonObject.class);
            String code = object.get("countryCode").getAsString();
            resp.close();
            return code.equals("CN") || code.equals("HK") || code.equals("MO") || code.equals("TW");
        } catch (Exception e) {
            return false;
        }
    }
}
