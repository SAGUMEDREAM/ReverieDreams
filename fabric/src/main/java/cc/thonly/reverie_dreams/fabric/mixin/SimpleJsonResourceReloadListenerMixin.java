package cc.thonly.reverie_dreams.fabric.mixin;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ReverieDreamsConfiguration;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.StrictJsonParser;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.Reader;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Mixin(SimpleJsonResourceReloadListener.class)
public class SimpleJsonResourceReloadListenerMixin {
    @Inject(
            method = "scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V",
            at = @At("HEAD")
    )
    private static <T> void injectTestObjectFileRead(ResourceManager manager, FileToIdConverter lister, DynamicOps<JsonElement> ops, Codec<T> codec, Map<Identifier, T> result, CallbackInfo ci) {
        ReverieDreamsConfiguration config = ReverieDreams.config();
        for (Map.Entry<Identifier, Resource> entry : lister.listMatchingResources(manager).entrySet()) {
            Identifier location = entry.getKey();
            Identifier id = lister.fileToId(location);
            try (Reader reader = entry.getValue().openAsReader()) {
                T orThrow = codec.parse(ops, StrictJsonParser.parse(reader)).getOrThrow();
            } catch (Exception e) {
                if (config.reportResourceReloadError) {
                    log.error("Error file {}", location, e);
                }
            }
        }
    }
}
