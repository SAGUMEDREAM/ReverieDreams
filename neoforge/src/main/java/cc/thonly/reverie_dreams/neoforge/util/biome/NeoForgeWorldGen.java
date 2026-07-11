package cc.thonly.reverie_dreams.neoforge.util.biome;

import cc.thonly.keine.api.callback.DynamicRegistrySetupCallback;
import cc.thonly.keine.api.registry.DynamicRegistryView;
import cc.thonly.reverie_dreams.ReverieDreams;
import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import dev.architectury.hooks.level.biome.BiomeProperties;
import dev.architectury.registry.level.biome.BiomeModifications;
import dev.architectury.registry.level.biome.forge.BiomeModificationsImpl;
import dev.architectury.utils.GameInstance;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

@SuppressWarnings({"unchecked", "rawtypes", "unused", "OptionalUsedAsFieldOrParameterType", "FieldMayBeFinal", "MismatchedQueryAndUpdateOfCollection"})
@Slf4j
public class NeoForgeWorldGen {
    static List<Pair<Predicate<BiomeModifications.BiomeContext>, BiConsumer<BiomeModifications.BiomeContext, BiomeProperties.Mutable>>> ADDITIONS = Lists.newArrayList();
    static List<Pair<Predicate<BiomeModifications.BiomeContext>, BiConsumer<BiomeModifications.BiomeContext, BiomeProperties.Mutable>>> POST_PROCESSING = Lists.newArrayList();
    static List<Pair<Predicate<BiomeModifications.BiomeContext>, BiConsumer<BiomeModifications.BiomeContext, BiomeProperties.Mutable>>> REMOVALS = Lists.newArrayList();
    static List<Pair<Predicate<BiomeModifications.BiomeContext>, BiConsumer<BiomeModifications.BiomeContext, BiomeProperties.Mutable>>> REPLACEMENTS = Lists.newArrayList();
    public static final DeferredRegister<MapCodec<? extends BiomeModifier>> MODIFIERS_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, ReverieDreams.MOD_ID);
    public static final MapCodec<BiomeModifier> CODEC = MapCodec.unit(BiomeModifierImpl.INSTANCE);
    public static final DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<BiomeModifier>> SERIALIZER = MODIFIERS_SERIALIZERS.register("none_biome_mod_codec", () -> CODEC);

    public static void init(IEventBus modBus) {
        assertCheck();
        MODIFIERS_SERIALIZERS.register(modBus);
    }

    private static void assertCheck() {
        try {
            {
                Field field = BiomeModificationsImpl.class.getDeclaredField("ADDITIONS");
                field.setAccessible(true);
                ADDITIONS = (List) field.get(null);
            }
            {
                Field field = BiomeModificationsImpl.class.getDeclaredField("POST_PROCESSING");
                field.setAccessible(true);
                POST_PROCESSING = (List) field.get(null);
            }
            {
                Field field = BiomeModificationsImpl.class.getDeclaredField("REMOVALS");
                field.setAccessible(true);
                REMOVALS = (List) field.get(null);
            }
            {
                Field field = BiomeModificationsImpl.class.getDeclaredField("REPLACEMENTS");
                field.setAccessible(true);
                REPLACEMENTS = (List) field.get(null);
            }
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }


    public static BiomeModifications.BiomeContext wrapSelectionContext(Optional<ResourceKey<Biome>> biomeResourceKey, ModifiableBiomeInfo.BiomeInfo.Builder event) {
        return new BiomeModifications.BiomeContext() {
            BiomeProperties properties = new BiomeModificationsImpl.BiomeWrapped(event);

            @Override
            public Optional<Identifier> getKey() {
                return biomeResourceKey.map(ResourceKey::identifier);
            }

            @Override
            public BiomeProperties getProperties() {
                return properties;
            }

            @Override
            public boolean hasTag(TagKey<Biome> tag) {
                MinecraftServer server = GameInstance.getServer();
                if (server != null) {
                    Optional<? extends Registry<Biome>> registry = server.registryAccess().lookup(Registries.BIOME);
                    if (registry.isPresent()) {
                        Optional<Holder.Reference<Biome>> holder = registry.get().get(biomeResourceKey.get());
                        if (holder.isPresent()) {
                            return holder.get().is(tag);
                        }
                    }
                }
                return false;
            }
        };
    }
}
