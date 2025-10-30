package cc.thonly.reverie_dreams.util;

import cc.thonly.reverie_dreams.Touhou;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import javax.imageio.ImageIO;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ImageToTextScanner {
    private static final Map<Class<?>, ImageToTextScanner> INSTANCES = new Object2ObjectOpenHashMap<>();
    public static final LoaderFactory DEFAULT_FACTORY = (instance) -> {
        instance.loadImageFromJar(ofNamespace(Touhou.MOD_ID, "icon.png"));
        instance.loadImageFromJar(ofNamespace(Touhou.MOD_ID, "icon_about.png"));
    };

    private final Map<Integer, List<Component>> caches = new HashMap<>();
    private final Class<?> clazz;


    private ImageToTextScanner(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static ImageToTextScanner createInstance(Class<?> clazz) {
        return INSTANCES.computeIfAbsent(clazz, (x) -> new ImageToTextScanner(clazz));
    }

    public static void bootstrap() {
        CompletableFuture.runAsync(()-> {
            ImageToTextScanner instance = ImageToTextScanner.createInstance(Touhou.class);
            DEFAULT_FACTORY.onLoad(instance);
        });
    }

    public static String ofNamespace(String namespace, String filename) {
        return "/assets/" + namespace + "/" + filename;
    }

    public List<Component> renderImageToText(BufferedImage image, int width, int height) {
        int key = Objects.hash(width, height, imageHash(image));
        if (this.caches.containsKey(key)) {
            return this.caches.get(key);
        }

        List<Component> lines = new ArrayList<>();
        for (int y = 0; y < Math.min(height, image.getHeight()); y++) {
            MutableComponent line = Component.literal("");
            for (int x = 0; x < Math.min(width, image.getWidth()); x++) {
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xFF;
                if (alpha < 128) {
                    line.append(Component.literal(" ").setStyle(Style.EMPTY.withColor(ChatFormatting.BLACK)));
                    continue;
                }
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;
                line.append(
                        Component.literal("█").setStyle(
                                Style.EMPTY.withColor(TextColor.fromRgb((red << 16) | (green << 8) | blue))
                        )
                );
            }
            lines.add(line);
        }

        caches.put(key, lines);
        return lines;
    }

    public BufferedImage loadImageFromJar(String pathInJar) {
        try (InputStream stream = this.clazz.getResourceAsStream(pathInJar)) {
            if (stream == null) {
                throw new IllegalArgumentException("Resource not found in JAR: " + pathInJar);
            }
            return ImageIO.read(stream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load image from JAR: " + pathInJar, e);
        }
    }

    private int imageHash(BufferedImage image) {
        int hash = 7;
        for (int y = 0; y < image.getHeight(); y += 4) {
            for (int x = 0; x < image.getWidth(); x += 4) {
                hash = 31 * hash + image.getRGB(x, y);
            }
        }
        return hash;
    }

    public void clearCache() {
        caches.clear();
    }

    @FunctionalInterface
    public interface LoaderFactory {
        void onLoad(ImageToTextScanner instance);
    }
}
