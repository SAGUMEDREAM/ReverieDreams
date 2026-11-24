package cc.thonly.reverie_dreams.world.gensokyo.gen;

import cc.thonly.reverie_dreams.world.gen.RDBuiltinBiomes;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class GensokyoJunglePaletteTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }
    private static Holder<Biome> biome(ResourceKey<Biome> key) {
        return Holder.Reference.createStandAlone(new HolderOwner<Biome>() {}, key);
    }

    @Test
    void oldWorldPaletteReplacesOnlyTheAuthoredJungleSlotAndKeepsAllIndices() {
        var old = List.of(biome(Biomes.RIVER), biome(Biomes.BAMBOO_JUNGLE), biome(Biomes.BAMBOO_JUNGLE));
        var jungle = biome(RDBuiltinBiomes.GENSOKYO_JUNGLE);
        var current = List.of(Biomes.RIVER.identifier(), RDBuiltinBiomes.GENSOKYO_JUNGLE.identifier(), Biomes.BAMBOO_JUNGLE.identifier());
        var result = GensokyoBiomeSource.upgradeLegacyJungle(GensokyoChunkGenerator.DEFAULT_MAP, old, current, jungle);
        assertEquals(old.size(), result.size());
        assertSame(old.get(0), result.get(0)); assertSame(jungle, result.get(1)); assertSame(old.get(2), result.get(2));
    }

    @Test
    void customizedHolderAndOtherMapsKeepTheirSavedBiomeChoice() {
        var old = List.of(biome(Biomes.FOREST));
        var current = List.of(RDBuiltinBiomes.GENSOKYO_JUNGLE.identifier());
        var jungle = biome(RDBuiltinBiomes.GENSOKYO_JUNGLE);
        assertSame(old.getFirst(), GensokyoBiomeSource.upgradeLegacyJungle(
                GensokyoChunkGenerator.DEFAULT_MAP, old, current, jungle).getFirst());
        assertSame(old, GensokyoBiomeSource.upgradeLegacyJungle(Identifier.parse("example:other_map"), old, current, jungle));
    }
}
