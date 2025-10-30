package cc.thonly.reverie_dreams.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Slf4j
public class SkinFetcherCaches {
    private static final Path PATH = Paths.get("config", "reverie_dreams", "skin-cached.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final Codec<Map<String, Entry>> CODEC_MAP = Codec.unboundedMap(Codec.STRING, Entry.CODEC);
    public static final Map<String, Entry> MD5_CACHED = new Object2ObjectLinkedOpenHashMap<>();

    public static Property from(InputStream in) {
        if (MD5_CACHED.isEmpty()) {
            load();
        }
        String md5 = getMD5FromInputStream(in);
        if (md5 == null) {
            return null;
        }
        Entry entry = MD5_CACHED.get(md5);
        if (entry == null) {
            return null;
        }
        return entry.property();
    }

    public static void load() {
        try {
            if (!Files.exists(PATH.getParent())) {
                Files.createDirectories(PATH.getParent());
                Files.createFile(PATH);
                save();
            }
        } catch (IOException e) {
            log.error("创建目录或文件失败: {}", PATH, e);
        }

        if (!Files.exists(PATH)) {
            log.warn("缓存文件不存在，正在创建: {}", PATH);
            save();
            return;
        }

        File file = PATH.toFile();
        try (InputStream in = new FileInputStream(file)) {
            JsonElement element = JsonParser.parseReader(new InputStreamReader(in));
            DataResult<Map<String, Entry>> parse = CODEC_MAP.parse(JsonOps.INSTANCE, element);
            parse.result().ifPresent(map -> {
                MD5_CACHED.clear();
                MD5_CACHED.putAll(map);
            });
        } catch (Exception err) {
            log.error("加载缓存文件失败", err);
        }
    }

    // 保存缓存到文件
    public static void save() {
        DataResult<JsonElement> dataResult = CODEC_MAP.encodeStart(JsonOps.INSTANCE, MD5_CACHED);
        if (dataResult.isSuccess()) {
            JsonElement element = dataResult.getOrThrow();  // 获取编码后的JSON元素
            String json = GSON.toJson(element);  // 转换为JSON字符串
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(PATH.toFile()))) {
                GSON.toJson(json.getBytes(StandardCharsets.UTF_8), writer);  // 将JSON写入文件
            } catch (IOException e) {
                log.error("写入文件失败", e);
            }
        }
    }

    // 从输入流计算MD5
    public static String getMD5FromInputStream(InputStream in) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");

            byte[] buffer = new byte[1024];
            int bytesRead;

            // 读取输入流并更新MD5
            while ((bytesRead = in.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = md.digest();  // 获取MD5值

            // 将MD5值转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();

        } catch (IOException | NoSuchAlgorithmException e) {
            return null;  // 如果发生异常，返回null
        }
    }

    // Entry类用于表示皮肤条目
    public record Entry(Property property) {
        // 皮肤属性的Codec
        public static final Codec<Property> PROPERTY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Property::name),
                Codec.STRING.fieldOf("value").forGetter(Property::value),
                Codec.STRING.optionalFieldOf("signature", null).forGetter(Property::signature)
        ).apply(instance, Property::new));

        // Entry的Codec
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PROPERTY_CODEC.fieldOf("property").forGetter(Entry::property)
        ).apply(instance, Entry::new));
    }
}
