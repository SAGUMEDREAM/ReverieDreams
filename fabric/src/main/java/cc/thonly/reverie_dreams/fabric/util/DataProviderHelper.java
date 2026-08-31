package cc.thonly.reverie_dreams.fabric.util;

import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractRecipeTypeProvider;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
public class DataProviderHelper {
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static synchronized <R extends BaseRecipe> void outputFile(
            CachedOutput writer,
            Map<Identifier, ? extends AbstractRecipeTypeProvider.Factory<R>> factories,
            Function<AbstractRecipeTypeProvider.Factory<R>, Codec<R>> codecMapper,
            Function<AbstractRecipeTypeProvider.Factory<R>, Map<Identifier, R>> registryMapper,
            Function<AbstractRecipeTypeProvider.Factory<R>, String> dirnameMapper
    ) {
        try {
            for (AbstractRecipeTypeProvider.Factory<R> factory : factories.values()) {
                Codec<R> codec = codecMapper.apply(factory);
                Map<Identifier, R> registries = registryMapper.apply(factory);
                String dirname = dirnameMapper.apply(factory);

                outputFile(
                        writer,
                        registries,
                        codec,
                        dirname
                );
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    public static synchronized <T, R> void outputFile(CachedOutput writer,
                                                      Map<Identifier, T> registries,
                                                      Function<R, Codec<R>> codecMapper,
                                                      BiFunction<Identifier, T, R> mapper,
                                                      Function<R, String> dirnameMapper
    ) {
        Map<Identifier, R> newRegistries = new Object2ObjectLinkedOpenHashMap<>(128);
        for (Map.Entry<Identifier, T> mapEntry : registries.entrySet()) {
            R apply = mapper.apply(mapEntry.getKey(), mapEntry.getValue());
            Codec<R> codec = codecMapper.apply(apply);
            newRegistries.put(mapEntry.getKey(), apply);
            outputFile(writer, newRegistries, codec, dirnameMapper.apply(apply));
        }
    }

    public static synchronized <T, R> void outputFile(CachedOutput writer,
                                                      Map<Identifier, T> registries,
                                                      Codec<R> codec,
                                                      BiFunction<Identifier, T, R> mapper,
                                                      String dirname
    ) {
        Map<Identifier, R> newRegistries = new Object2ObjectLinkedOpenHashMap<>(128);
        for (Map.Entry<Identifier, T> mapEntry : registries.entrySet()) {
            R apply = mapper.apply(mapEntry.getKey(), mapEntry.getValue());
            newRegistries.put(mapEntry.getKey(), apply);
        }
        outputFile(writer, newRegistries, codec, dirname);
    }

    public static synchronized <T> void outputFile(CachedOutput writer,
                                                   Map<Identifier, T> registries,
                                                   Codec<T> codec,
                                                   String dirname
    ) {
        Path outputDir = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
        try {
            for (var entry : registries.entrySet()) {
                Identifier key = entry.getKey();
                T value = entry.getValue();
                DataResult<JsonElement> result = codec.encodeStart(JsonOps.INSTANCE, value);
                Optional<JsonElement> optional = result.result();
                Path path = DataGeneratorUtil.getData(outputDir, key.getNamespace(), dirname, null);
                if (optional.isPresent()) {
                    JsonElement element = optional.get();
                    Path filePath = path.resolve(key.getPath() + ".json");
                    String jsonString = gson.toJson(element);
                    byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
                    Files.createDirectories(filePath.getParent());

                    writer.writeIfNeeded(filePath, bytes, HashCode.fromBytes(bytes));
                }
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }
}
