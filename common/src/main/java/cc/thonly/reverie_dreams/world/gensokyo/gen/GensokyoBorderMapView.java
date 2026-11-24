package cc.thonly.reverie_dreams.world.gensokyo.gen;

import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapData;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.List;

/**
 * Adapts the existing immutable map; the terrain sampler never edits its layers.
 */
public record GensokyoBorderMapView(GensokyoMapData map,
                                    List<String> surfaces) implements GensokyoBorderTerrain.MapView {
    public static GensokyoBorderMapView of(GensokyoMapData map) {
        return new GensokyoBorderMapView(map, map.surfacePalette().stream()
                .map(state -> BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString()).toList());
    }

    @Override
    public int width() {
        return map.width();
    }

    @Override
    public int depth() {
        return map.depth();
    }

    @Override
    public int originX() {
        return map.originX();
    }

    @Override
    public int originZ() {
        return map.originZ();
    }

    @Override
    public int minY() {
        return map.minY();
    }

    @Override
    public int worldHeight() {
        return map.worldHeight();
    }

    @Override
    public int seaLevel() {
        return map.seaLevel();
    }

    @Override
    public int heightAt(int x, int z) {
        return map.heightAt(x, z);
    }

    @Override
    public String surfaceAt(int x, int z) {
        int index = map.surfaceIndexAt(x, z);
        return index < surfaces.size() ? surfaces.get(index) : "minecraft:grass_block";
    }

    @Override
    public String biomeAt(int x, int z) {
        int index = map.biomeIndexAt(x, z);
        return index < map.biomePalette().size() ? map.biomePalette().get(index).toString() : "minecraft:plains";
    }
}
