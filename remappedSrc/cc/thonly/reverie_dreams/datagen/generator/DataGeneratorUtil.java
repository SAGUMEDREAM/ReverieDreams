package cc.thonly.reverie_dreams.datagen.generator;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Slf4j
public class DataGeneratorUtil {
    public static final String ASSETS = "assets";
    public static final String DATA = "data";
    public static String OUTPUT_DIR;

    static {
        try {
            Class<?> clazz = Class.forName("net.fabricmc.fabric.impl.datagen.FabricDataGenHelper");
            Field field = clazz.getDeclaredField("OUTPUT_DIR");
            field.setAccessible(true);
            OUTPUT_DIR = (String) field.get(null);
            Objects.requireNonNull(OUTPUT_DIR, "No output dir provided with the 'fabric-api.datagen.output-dir' property");
        } catch (Exception err) {
            String fadod = System.getProperty("fabric-api.datagen.output-dir");
            if (fadod == null) {
                log.error("Can't get output dir: ", err);
                OUTPUT_DIR = null;
            } else {
                OUTPUT_DIR = fadod;
            }
        }
    }

    public static Path getAssets(Path output, String namespace, ResourceKey<?> key, @Nullable List<String> other) {
        return getOutput(output, namespace, key, other, ASSETS);
    }

    public static Path getData(Path output, String namespace, ResourceKey<?> key, @Nullable List<String> other) {
        return getOutput(output, namespace, key, other, DATA);
    }

    public static Path getAssets(Path output, String namespace, String key, @Nullable List<String> other) {
        return getOutput(output, namespace, key, other, ASSETS);
    }

    public static Path getData(Path output, String namespace, String key, @Nullable List<String> other) {
        return getOutput(output, namespace, key, other, DATA);
    }

    public static Path getAssetsByNullable(Path output, String namespace, ResourceKey<?> key, @Nullable List<String> other) {
        return getOutput(output, namespace, key, other, ASSETS);
    }

    public static Path getDataByNullable(Path output, String namespace, ResourceKey<?> key, @Nullable List<String> other) {
        return getOutput(output, namespace, key, other, DATA);
    }

    public static Path getOutput(Path output, String namespace, ResourceKey<?> key, @Nullable List<String> other, String type) {
        Path base = output.resolve(type).resolve(namespace);
        if (key != null) {
            base = base.resolve(key.location().getPath());
        }
        if (other != null) {
            for (String string : other) {
                base = base.resolve(string);
            }
        }
        return base;
    }

    public static Path getOutput(Path output, String namespace, String name, @Nullable List<String> other, String type) {
        Path base = output.resolve(type).resolve(namespace);
        if (name != null) {
            base = base.resolve(name);
        }
        if (other != null) {
            for (String string : other) {
                base = base.resolve(string);
            }
        }
        return base;
    }
}
