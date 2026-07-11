package cc.thonly.reverie_dreams.neoforge.util.biome;

import com.mojang.serialization.MapCodec;
import dev.architectury.hooks.level.biome.BiomeProperties;
import dev.architectury.registry.level.biome.BiomeModifications;
import dev.architectury.registry.level.biome.forge.BiomeModificationsImpl;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class BiomeModifierImpl implements BiomeModifier {
    public static final BiomeModifier INSTANCE = new BiomeModifierImpl();

    @Override
    public void modify(Holder<Biome> arg, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        List<Pair<Predicate<BiomeModifications.BiomeContext>, BiConsumer<BiomeModifications.BiomeContext, BiomeProperties.Mutable>>> list = switch (phase) {
            case ADD -> NeoForgeWorldGen.ADDITIONS;
            case REMOVE -> NeoForgeWorldGen.REMOVALS;
            case MODIFY -> NeoForgeWorldGen.REPLACEMENTS;
            case AFTER_EVERYTHING -> NeoForgeWorldGen.POST_PROCESSING;
            default -> null;
        };

        if (list == null) {
            return;
        }
        System.out.println(arg);
        BiomeModifications.BiomeContext biomeContext = NeoForgeWorldGen.wrapSelectionContext(arg.unwrapKey(), builder);
        BiomeProperties.Mutable mutableBiome = new BiomeModificationsImpl.MutableBiomeWrapped(builder);
        for (var pair : list) {
            if (pair.getLeft().test(biomeContext)) {
                pair.getRight().accept(biomeContext, mutableBiome);
            }
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return NeoForgeWorldGen.CODEC;
    }
}
