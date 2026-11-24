package cc.thonly.reverie_dreams.world.moon.compat;

import cc.thonly.reverie_dreams.ReverieDreams;

import net.minecraft.nbt.*;

import java.util.HashSet;
import java.util.Set;

/**
 * NBT-only conversion: all existing block sections and gameplay NBT are retained verbatim.
 */
public final class MoonChunkUpgrade {
    public static final String MARKER = ReverieDreams.MOD_ID + ":moon_terrain_version";

    private MoonChunkUpgrade() {
    }

    public static boolean upgrade(CompoundTag chunk) {
        if (chunk.getIntOr(MARKER, 0) >= 2 || chunk.getIntOr("yPos", -2) == -8)
            return false;
        if (chunk.getIntOr("yPos", -2) != -2)
            throw new IllegalStateException("Unsupported Moon chunk minimum section");
        ListTag sections = chunk.getListOrEmpty("sections");
        Set<Integer> existing = new HashSet<>();
        // Old light-only border sections are not terrain and must not leave void gaps below the old floor.
        sections.removeIf(entry -> entry instanceof CompoundTag section
                && section.getIntOr("Y", 0) < -2 && !section.contains("block_states"));
        for (Tag entry : sections)
            if (entry instanceof CompoundTag section)
                existing.add(section.getIntOr("Y", 0));
        for (int y = -8; y < -2; y++) {
            if (existing.contains(y))
                continue;
            CompoundTag section = new CompoundTag();
            section.putByte("Y", (byte) y);
            CompoundTag blocks = new CompoundTag();
            ListTag palette = new ListTag();
            CompoundTag state = new CompoundTag();
            state.putString("Name", y == -8 ? "minecraft:bedrock" : ReverieDreams.MOD_ID + ":moon_stone");
            palette.add(state);
            blocks.put("palette", palette);
            section.put("block_states", blocks);
            CompoundTag biomes = new CompoundTag();
            ListTag biomePalette = new ListTag();
            biomePalette.add(StringTag.valueOf(ReverieDreams.MOD_ID + ":the_moon"));
            biomes.put("palette", biomePalette);
            section.put("biomes", biomes);
            // Bottom section has only a one-block bedrock floor; its remaining blocks are moon stone.
            if (y == -8) {
                CompoundTag stone = new CompoundTag();
                stone.putString("Name", ReverieDreams.MOD_ID + ":moon_stone");
                palette.add(stone);
                long[] packed = new long[256];
                for (int block = 256; block < 4096; block++)
                    packed[block / 16] |= 1L << (block % 16 * 4);
                blocks.putLongArray("data", packed);
            }
            sections.add(section);
        }
        chunk.put("sections", sections);
        chunk.remove("Heightmaps"); // Heightmaps encode offsets from minY; let vanilla rebuild with -128.
        chunk.putBoolean("isLightOn", false);
        for (Tag entry : sections)
            if (entry instanceof CompoundTag section) {
                section.remove("BlockLight");
                section.remove("SkyLight");
            }
        if (chunk.contains("PostProcessing")) {
            ListTag old = chunk.getListOrEmpty("PostProcessing"), shifted = new ListTag();
            for (int i = 0; i < 6; i++)
                shifted.add(new ListTag());
            for (Tag entry : old)
                shifted.add(entry.copy());
            chunk.put("PostProcessing", shifted);
        }
        if (chunk.contains("UpgradeData")) {
            CompoundTag upgrade = chunk.getCompoundOrEmpty("UpgradeData");
            if (upgrade.contains("Indices")) {
                CompoundTag indices = upgrade.getCompoundOrEmpty("Indices"), shifted = new CompoundTag();
                for (String key : indices.keySet()) {
                    try {
                        shifted.put(Integer.toString(Integer.parseInt(key) + 6), indices.get(key).copy());
                    } catch (NumberFormatException unsupported) {
                        throw new IllegalStateException("Invalid Moon UpgradeData section index: " + key, unsupported);
                    }
                }
                upgrade.put("Indices", shifted);
            }
        }
        if (chunk.contains("carving_mask")) {
            long[] old = chunk.getLongArray("carving_mask").orElseThrow();
            long[] shifted = new long[old.length + 384]; // 96 blocks * 256 cells / 64 bits
            System.arraycopy(old, 0, shifted, 384, old.length);
            chunk.putLongArray("carving_mask", shifted);
        }
        chunk.putInt("yPos", -8);
        chunk.putInt(MARKER, 2);
        return true;
    }
}
