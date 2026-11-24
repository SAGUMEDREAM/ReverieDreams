package cc.thonly.reverie_dreams.util;

import cc.thonly.reverie_dreams.api.registry.tag.ConventionalBlockTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

/**
 * 探宝棒使用的矿物判定与描边配色工具。
 * <p>配色只负责 RGB，写入 Display 发光颜色时再补上不透明的 alpha。</p>
 */
public final class OreDetector {
    /**
     * 判定为“矿物”的方块标签。
     */
    public static final List<TagKey<Block>> ORE_TAGS = List.of(
            BlockTags.COAL_ORES,
            BlockTags.COPPER_ORES,
            BlockTags.DIAMOND_ORES,
            BlockTags.EMERALD_ORES,
            BlockTags.GOLD_ORES,
            BlockTags.IRON_ORES,
            BlockTags.LAPIS_ORES,
            BlockTags.REDSTONE_ORES,
            ConventionalBlockTags.ORES,
            ConventionalBlockTags.QUARTZ_ORES,
            ConventionalBlockTags.NETHERITE_SCRAP_ORES
    );

    /**
     * 找不到对应颜色时使用的兜底色。
     */
    public static final int DEFAULT_COLOR = 0xE8E8E8;

    private static final Map<String, Integer> MOD_ORE_COLORS = Map.ofEntries(
            Map.entry("silver_ore", 0xD6DEE4),
            Map.entry("deepslate_silver_ore", 0xD6DEE4),
            Map.entry("orb_ore", 0x9C86FF),
            Map.entry("deepslate_orb_ore", 0x9C86FF),
            Map.entry("moon_iron_ore", 0xC6D2F0),
            Map.entry("moon_gold_ore", 0xFFE28A),
            Map.entry("moon_quartz_ore", 0xF3EDE3),
            Map.entry("moon_diamond_ore", 0x9BF0E8),
            Map.entry("dream_crystal_ore", 0xFF8FD8)
    );

    private OreDetector() {
    }

    public static boolean isOre(BlockState state) {
        if (state.isAir()) {
            return false;
        }
        for (TagKey<Block> tag : ORE_TAGS) {
            if (state.is(tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 按矿种返回描边颜色（0xRRGGBB）。
     */
    public static int colorFor(BlockState state) {
        Block block = state.getBlock();
        var key = BuiltInRegistries.BLOCK.getKey(block);
        if (key != null && "reverie_dreams".equals(key.getNamespace())) {
            Integer color = MOD_ORE_COLORS.get(key.getPath());
            if (color != null) {
                return color;
            }
        }
        if (state.is(BlockTags.DIAMOND_ORES)) {
            return 0x4AEDD9;
        }
        if (state.is(BlockTags.EMERALD_ORES)) {
            return 0x17DD62;
        }
        if (state.is(BlockTags.LAPIS_ORES)) {
            return 0x3A5BFF;
        }
        if (state.is(BlockTags.REDSTONE_ORES)) {
            return 0xFF3B3B;
        }
        if (state.is(BlockTags.GOLD_ORES)) {
            return 0xFCEE4B;
        }
        if (state.is(BlockTags.IRON_ORES)) {
            return 0xE0B096;
        }
        if (state.is(BlockTags.COPPER_ORES)) {
            return 0xE07A3F;
        }
        if (state.is(BlockTags.COAL_ORES)) {
            return 0x9AA0A6;
        }
        if (state.is(ConventionalBlockTags.QUARTZ_ORES)) {
            return 0xF2ECE4;
        }
        if (state.is(ConventionalBlockTags.NETHERITE_SCRAP_ORES)) {
            return 0x8C6E5F;
        }
        return DEFAULT_COLOR;
    }
}
