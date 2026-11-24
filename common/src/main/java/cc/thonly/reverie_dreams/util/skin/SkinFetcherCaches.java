package cc.thonly.reverie_dreams.util.skin;

import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class SkinFetcherCaches {
    private static final SkinCacheStore STORE = SkinCacheStore.defaultStore();
    public static final Codec<Map<String, Entry>> CODEC_MAP = Codec.unboundedMap(Codec.STRING, Entry.CODEC);
    public static final Map<String, Entry> MD5_CACHED = Collections.synchronizedMap(new Object2ObjectLinkedOpenHashMap<>());
    private static boolean loaded;

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

    public static synchronized void load() {
        if (loaded) {
            return;
        }
        loaded = true;
        MD5_CACHED.clear();
        MD5_CACHED.putAll(STORE.load());
    }

    public static synchronized void save() {
        loaded = true;
        Map<String, Entry> snapshot;
        synchronized (MD5_CACHED) {
            snapshot = new LinkedHashMap<>(MD5_CACHED);
        }
        STORE.save(snapshot);
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
                Codec.STRING.optionalFieldOf("signature", "").forGetter(Property::signature)
        ).apply(instance, Property::new));

        // Entry的Codec
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PROPERTY_CODEC.fieldOf("property").forGetter(Entry::property)
        ).apply(instance, Entry::new));
    }
}
