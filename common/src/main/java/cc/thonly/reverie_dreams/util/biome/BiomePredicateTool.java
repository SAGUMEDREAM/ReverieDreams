package cc.thonly.reverie_dreams.util.biome;

import net.blay09.mods.balm.world.level.biome.BiomePredicate;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.*;

public class BiomePredicateTool {

    public static BiomePredicate all() {
        return biomeHolder -> true;
    }

    public static BiomePredicate vanilla() {
        return biomeHolder -> biomeHolder.unwrapKey()
                .map(key -> key.identifier().getNamespace().equals("minecraft")
                        && BuiltInRegistryKeys.isBuiltinBiome(key))
                .orElse(false);
    }

    // ===== include =====

    @SafeVarargs
    public static BiomePredicate includeByKey(TagKey<Biome>... tags) {
        return biomeHolder -> Arrays.stream(tags).anyMatch(biomeHolder::is);
    }

    @SafeVarargs
    public static BiomePredicate includeByKey(ResourceKey<Biome>... keys) {
        return biomeHolder -> Arrays.stream(keys).anyMatch(biomeHolder::is);
    }

    public static BiomePredicate includeByKey(Collection<ResourceKey<Biome>> keys) {
        return biomeHolder -> biomeHolder.unwrapKey()
                .map(keys::contains)
                .orElse(false);
    }

    // ===== exclude =====

    @SafeVarargs
    public static BiomePredicate excludeByKey(ResourceKey<Biome>... keys) {
        return excludeByKey(Set.of(keys));
    }

    public static BiomePredicate excludeByKey(Collection<ResourceKey<Biome>> keys) {
        return biomeHolder -> biomeHolder.unwrapKey()
                .map(key -> !keys.contains(key))
                .orElse(true);
    }

    // ===== tag =====

    public static BiomePredicate tag(TagKey<Biome> tag) {
        return biomeHolder -> biomeHolder.is(tag);
    }

    // ===== spawn check =====
    public static BiomePredicate spawnsOneOf(EntityType<?>... entityTypes) {
        return spawnsOneOf(Set.of(entityTypes));
    }

    public static BiomePredicate spawnsOneOf(Set<EntityType<?>> entityTypes) {
        return biomeHolder -> {
            Biome biome = biomeHolder.value();
            MobSpawnSettings settings = biome.getMobSettings();

            for (MobCategory category : MobCategory.values()) {
                var list = settings.getMobs(category);

                for (var entry : list.unwrap()) {
                    if (entityTypes.contains(entry.value().type())) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    public static BiomePredicate foundInOverworld(RegistryAccess registryAccess) {
        return foundInDimension(registryAccess, LevelStem.OVERWORLD);
    }

    public static BiomePredicate foundInTheNether(RegistryAccess registryAccess) {
        return foundInDimension(registryAccess, LevelStem.NETHER);
    }

    public static BiomePredicate foundInTheEnd(RegistryAccess registryAccess) {
        return foundInDimension(registryAccess, LevelStem.END);
    }

    public static BiomePredicate foundInDimension(RegistryAccess registryAccess, ResourceKey<LevelStem> dimensionKey) {
        return biomeHolder -> {
            var levelStemRegistry = registryAccess.lookupOrThrow(Registries.LEVEL_STEM);
            LevelStem levelStem = levelStemRegistry.getValue(dimensionKey);

            if (levelStem == null) {
                return false;
            }

            return levelStem.generator()
                    .getBiomeSource()
                    .possibleBiomes()
                    .stream()
                    .anyMatch(holder -> holder.value() == biomeHolder.value());
        };
    }
}